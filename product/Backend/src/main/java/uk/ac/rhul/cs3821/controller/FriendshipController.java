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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.FriendshipDto;
import uk.ac.rhul.cs3821.dto.UserSummaryDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.FriendshipService;

/**
 * Controller manages friendships between users.
 * Supports sending, accepting, and removing friendships, as well as user search.
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

  private final FriendshipService friendshipService;
  private final UserRepository userRepository;

  /**
   * Resolves the currently authenticated user from the security context.
   *
   * @return the authenticated user
   * @throws EntityNotFoundException if the user is not found
   */
  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  /**
   * Sends a friend request to the specified user.
   *
   * @param addresseeId the id of the user to send the request to
   * @return the created FriendshipDto
   */
  @PostMapping("/request/{addresseeId}")
  public ResponseEntity<FriendshipDto> sendRequest(@PathVariable Long addresseeId) {
    Long requesterId = getCurrentUser().getId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(friendshipService.sendRequest(requesterId, addresseeId));
  }

  /**
   * Accepts an incoming friend request. Only the address may accept.
   *
   * @param friendshipId the id of the friendship to accept
   * @return the updated FriendshipDto
   */
  @PutMapping("/accept/{friendshipId}")
  public ResponseEntity<FriendshipDto> acceptRequest(@PathVariable Long friendshipId) throws AccessDeniedException {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.acceptRequest(friendshipId, userId));
  }

  /**
   * Removes a friendship or rejects a pending request. Only a participant in the friendship may perform this action.
   *
   * @param friendshipId the id of the friendship to accept
   * @return the updated FriendshipDto
   */
  @DeleteMapping("/{friendshipId}")
  public ResponseEntity<Void> removeFriend(@PathVariable Long friendshipId) throws AccessDeniedException {
    Long userId = getCurrentUser().getId();
    friendshipService.removeFriend(friendshipId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns all accepted friends for the current user.
   *
   * @return list of UserSummaryDto representing each friend
   */
  @GetMapping
  public ResponseEntity<List<UserSummaryDto>> getMyFriends() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.getFriends(userId));
  }

  /**
   * Returns all incoming pending friend requests for the current user.
   *
   * @return list of FriendshipDto with status PENDING
   */
  @GetMapping("/pending")
  public ResponseEntity<List<FriendshipDto>> getPendingRequests() {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
  }

  /**
   * Searches for users by email. Used to find users to send friend requests to.
   *
   * @param query partial email string to search by
   * @return list of matching UserSummaryDto
   */
  @GetMapping("/search")
  public ResponseEntity<List<UserSummaryDto>> searchUsers(
      @RequestParam String query) {
    return ResponseEntity.ok(friendshipService.searchUsers(query));
  }
}