package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.NotificationDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final UserRepository userRepository;

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  // All notifications — frontend uses this to populate the notification panel
  @GetMapping
  public ResponseEntity<List<NotificationDto>> getAll() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getAllForUser(userId));
  }

  // Unread only — poll this every 30s for the bell badge
  @GetMapping("/unread")
  public ResponseEntity<List<NotificationDto>> getUnread() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getUnreadForUser(userId));
  }

  // Just the count — lightweight endpoint to drive the badge number
  @GetMapping("/unread/count")
  public ResponseEntity<Integer> getUnreadCount() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getUnreadCount(userId));
  }

  // Mark a single notification as read
  @PutMapping("/{notificationId}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) throws Exception {
    Long userId = getCurrentUser().getId();
    notificationService.markAsRead(notificationId, userId);
    return ResponseEntity.noContent().build();
  }

  // Mark all as read — call this when user opens the notification panel
  @PutMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {
    Long userId = getCurrentUser().getId();
    notificationService.markAllAsRead(userId);
    return ResponseEntity.noContent().build();
  }
}