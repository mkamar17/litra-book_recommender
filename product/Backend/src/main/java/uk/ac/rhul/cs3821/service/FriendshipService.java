package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.dto.FriendshipDto;
import uk.ac.rhul.cs3821.dto.UserSummaryDto;
import uk.ac.rhul.cs3821.model.Friendship;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.FriendshipRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Service for managing friendships between users.
 * Handles sending, accepting, and removing friendships, as well as user search.
 */
@Service
@Transactional
public class FriendshipService {

  private final FriendshipRepository friendshipRepo;
  private final NotificationService notificationService;
  private final UserRepository userRepo;

  /**
   * Constructs a FriendshipService with all required dependencies.
   *
   * @param friendshipRepo      repository for friendship entities
   * @param notificationService service for sending notifications
   * @param userRepo            repository for user entities
   */
  public FriendshipService(FriendshipRepository friendshipRepo,
                           NotificationService notificationService,
                           UserRepository userRepo) {
    this.friendshipRepo = friendshipRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
  }

  /**
   * Sends a friend request from one user to another.
   * Notifies the addressee on success.
   *
   * @param requesterId the ID of the user sending the request
   * @param addresseeId the ID of the user receiving the request
   * @return the created FriendshipDto with status PENDING
   * @throws IllegalArgumentException if the user tries to friend themselves
   * @throws IllegalStateException    if a friendship already exists between the two users
   * @throws EntityNotFoundException  if the addressee is not found
   */
  public FriendshipDto sendRequest(Long requesterId, Long addresseeId) {
    if (requesterId.equals(addresseeId)) {
      throw new IllegalArgumentException("Cannot send request to yourself");
    }

    friendshipRepo.findBetweenUsers(requesterId, addresseeId).ifPresent(f -> {
      throw new IllegalStateException("Friendship already exists with status: " + f.getStatus());
    });

    User requester = userRepo.getReferenceById(requesterId);
    User addressee = userRepo.findById(addresseeId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Friendship friendship = new Friendship();
    friendship.setRequester(requester);
    friendship.setAddressee(addressee);
    friendship.setStatus(FriendshipStatus.PENDING);

    Friendship saved = friendshipRepo.save(friendship);
    notificationService.send(addresseeId, NotificationType.FRIEND_REQUEST, saved.getId());

    return toDto(saved, addressee);
  }

  /**
   * Accepts a pending friend request. Only the addressee may accept.
   *
   * @param friendshipId  the ID of the friendship to accept
   * @param currentUserId the ID of the currently authenticated user
   * @return the updated FriendshipDto with status ACCEPTED
   * @throws EntityNotFoundException if the friendship is not found
   * @throws AccessDeniedException   if the current user is not the addressee
   */
  public FriendshipDto acceptRequest(Long friendshipId, Long currentUserId) {
    Friendship friendship = friendshipRepo.findById(friendshipId)
        .orElseThrow(() -> new EntityNotFoundException("Request not found"));

    if (!friendship.getAddressee().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Not your request to accept");
    }

    friendship.setStatus(FriendshipStatus.ACCEPTED);
    Friendship saved = friendshipRepo.save(friendship);

    return toDto(saved, friendship.getRequester());
  }

  /**
   * Removes a friendship or rejects a pending request.
   * Either participant in the friendship may perform this action.
   *
   * @param friendshipId  the ID of the friendship to remove
   * @param currentUserId the ID of the currently authenticated user
   * @throws EntityNotFoundException if the friendship is not found
   * @throws AccessDeniedException   if the current user is not part of the friendship
   */
  public void removeFriend(Long friendshipId, Long currentUserId) {
    Friendship friendship = friendshipRepo.findById(friendshipId)
        .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

    boolean isRequester = friendship.getRequester().getId().equals(currentUserId);
    boolean isAddressee = friendship.getAddressee().getId().equals(currentUserId);

    if (!isRequester && !isAddressee) {
      throw new AccessDeniedException("You are not part of this friendship");
    }
    
    friendshipRepo.delete(friendship);
  }

  /**
   * Returns all accepted friends for a user as summary DTOs.
   * Resolves the other person in each friendship regardless of requester or addressee.
   *
   * @param userId the ID of the user
   * @return list of UserSummaryDto representing each friend
   */
  public List<UserSummaryDto> getFriends(Long userId) {
    return friendshipRepo.findAllAcceptedFriendships(userId).stream()
        .map(f -> {
          User friend = f.getRequester().getId().equals(userId)
              ? f.getAddressee()
              : f.getRequester();
          return new UserSummaryDto(friend.getId(), friend.getEmail(), friend.getTotalPoints());
        })
        .toList();
  }

  /**
   * Returns all incoming pending friend requests for a user.
   *
   * @param userId the ID of the user
   * @return list of FriendshipDto with status PENDING
   */
  public List<FriendshipDto> getPendingRequests(Long userId) {
    return friendshipRepo.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
        .stream()
        .map(f -> toDto(f, f.getRequester()))
        .toList();
  }

  /**
   * Searches for users by partial email match.
   * Used to find users to send friend requests to.
   *
   * @param query partial email string to search by
   * @return list of matching UserSummaryDto
   */
  public List<UserSummaryDto> searchUsers(String query) {
    return userRepo.findByEmailContainingIgnoreCase(query)
        .stream()
        .map(u -> new UserSummaryDto(u.getId(), u.getEmail(), u.getTotalPoints()))
        .toList();
  }

  /**
   * Maps a Friendship entity to a FriendshipDto.
   * The otherUser parameter represents the person on the other side of the friendship.
   *
   * @param f         the friendship entity
   * @param otherUser the other user in the friendship
   * @return the mapped FriendshipDto
   */
  private FriendshipDto toDto(Friendship f, User otherUser) {
    return new FriendshipDto(f.getId(), otherUser.getId(),
        otherUser.getEmail(), f.getStatus());
  }
}