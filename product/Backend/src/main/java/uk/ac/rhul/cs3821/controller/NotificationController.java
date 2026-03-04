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

/**
 * Controller manages user notifications.
 * Provides endpoints for retrieving and marking notifications as read.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final UserRepository userRepository;

  /**
   * Resolves the currently authenticated user from the security context.
   *
   * @return the authenticated User
   * @throws EntityNotFoundException if the user is not found
   */
  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  /**
   * Returns all notifications for the current user, newest first.
   * Used to populate the full notification panel on the frontend.
   *
   * @return list of all NotificationDto for the user
   */
  @GetMapping
  public ResponseEntity<List<NotificationDto>> getAll() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getAllForUser(userId));
  }

  /**
   * Returns only unread notifications for the current user.
   * Intended to be polled periodically to update the bell indicator.
   *
   * @return list of unread NotificationDto
   */
  @GetMapping("/unread")
  public ResponseEntity<List<NotificationDto>> getUnread() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getUnreadForUser(userId));
  }

  /**
   * Returns the count of unread notifications for the current user.
   * Lightweight endpoint used to drive the notification badge number.
   *
   * @return integer count of unread notifications
   */
  @GetMapping("/unread/count")
  public ResponseEntity<Integer> getUnreadCount() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(notificationService.getUnreadCount(userId));
  }

  /**
   * Marks a single notification as read. Only the recipient may mark their own notification.
   *
   * @param notificationId the ID of the notification to mark as read
   * @return HTTP 204 on success
   */
  @PutMapping("/{notificationId}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) throws Exception {
    Long userId = getCurrentUser().getId();
    notificationService.markAsRead(notificationId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Marks all notifications as read for the current user.
   * Typically called when the user opens the notification panel.
   *
   * @return HTTP 204 on success
   */
  @PutMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {
    Long userId = getCurrentUser().getId();
    notificationService.markAllAsRead(userId);
    return ResponseEntity.noContent().build();
  }
}