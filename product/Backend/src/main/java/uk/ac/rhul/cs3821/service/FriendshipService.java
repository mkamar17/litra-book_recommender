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

@Service
@Transactional
public class FriendshipService {

  private final FriendshipRepository friendshipRepo;
  private final NotificationService notificationService;
  private final UserRepository userRepo;

  public FriendshipService(FriendshipRepository friendshipRepo,
                           NotificationService notificationService,
                           UserRepository userRepo) {
    this.friendshipRepo = friendshipRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
  }

  public FriendshipDto sendRequest(Long requesterId, Long addresseeId) {
    if (requesterId.equals(addresseeId))
      throw new IllegalArgumentException("Cannot send request to yourself");

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

  public FriendshipDto acceptRequest(Long friendshipId, Long currentUserId) {
    Friendship friendship = friendshipRepo.findById(friendshipId)
        .orElseThrow(() -> new EntityNotFoundException("Request not found"));

    if (!friendship.getAddressee().getId().equals(currentUserId))
      throw new AccessDeniedException("Not your request to accept");

    friendship.setStatus(FriendshipStatus.ACCEPTED);
    Friendship saved = friendshipRepo.save(friendship);

    return toDto(saved, friendship.getRequester());
  }

  public void removeFriend(Long friendshipId, Long currentUserId) {
    Friendship friendship = friendshipRepo.findById(friendshipId)
        .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

    boolean isRequester = friendship.getRequester().getId().equals(currentUserId);
    boolean isAddressee = friendship.getAddressee().getId().equals(currentUserId);

    if (!isRequester && !isAddressee)
      throw new AccessDeniedException("You are not part of this friendship");

    friendshipRepo.delete(friendship);
  }

  public List<UserSummaryDto> getFriends(Long userId) {
    return friendshipRepo.findAllAcceptedFriendships(userId).stream()
        .map(f -> {
          // Return the OTHER person in the friendship, not the current user
          User friend = f.getRequester().getId().equals(userId)
              ? f.getAddressee()
              : f.getRequester();
          return new UserSummaryDto(friend.getId(), friend.getEmail(), friend.getTotalPoints());
        })
        .toList();
  }

  public List<FriendshipDto> getPendingRequests(Long userId) {
    return friendshipRepo.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
        .stream()
        .map(f -> toDto(f, f.getRequester()))
        .toList();
  }

  // Maps a Friendship to a DTO — otherUser is the person on the other side
  private FriendshipDto toDto(Friendship f, User otherUser) {
    return new FriendshipDto(f.getId(), otherUser.getId(),
        otherUser.getEmail(), f.getStatus());
  }
}