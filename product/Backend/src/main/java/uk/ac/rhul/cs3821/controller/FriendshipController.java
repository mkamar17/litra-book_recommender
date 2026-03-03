package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.FriendshipDto;
import uk.ac.rhul.cs3821.dto.UserSummaryDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.FriendshipService;

/**
 * Controller handles actions user can do for the friends feature.
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

  private final FriendshipService friendshipService;
  private final UserRepository userRepository;

  // Reusable — mirrors exactly what UserController does
  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @PostMapping("/request/{addresseeId}")
  public ResponseEntity<FriendshipDto> sendRequest(@PathVariable Long addresseeId) {
    Long requesterId = getCurrentUser().getId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(friendshipService.sendRequest(requesterId, addresseeId));
  }

  @PutMapping("/accept/{friendshipId}")
  public ResponseEntity<FriendshipDto> acceptRequest(@PathVariable Long friendshipId) throws AccessDeniedException {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.acceptRequest(friendshipId, userId));
  }

  @DeleteMapping("/{friendshipId}")
  public ResponseEntity<Void> removeFriend(@PathVariable Long friendshipId) throws AccessDeniedException {
    Long userId = getCurrentUser().getId();
    friendshipService.removeFriend(friendshipId, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<UserSummaryDto>> getMyFriends() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.getFriends(userId));
  }

  @GetMapping("/pending")
  public ResponseEntity<List<FriendshipDto>> getPendingRequests() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
  }
}