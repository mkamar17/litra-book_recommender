package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import uk.ac.rhul.cs3821.model.Friendship;
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

  public FriendshipService(FriendshipRepository friendshipRepo, NotificationService notificationService, UserRepository userRepo) {
    this.friendshipRepo = friendshipRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
  }

  public Friendship sendRequest(Long requesterId, Long addresseeId) {
    // Guard: can't friend yourself
    if (requesterId.equals(addresseeId))
      throw new IllegalArgumentException("Cannot send request to yourself");

    // Guard: relationship already exists
    friendshipRepo.findBetweenUsers(requesterId, addresseeId).ifPresent(f -> {
      throw new IllegalStateException("Friendship already exists with status: " + f.getStatus());
    });

    Friendship friendship = new Friendship();
    friendship.setRequester(userRepo.getReferenceById(requesterId));
    friendship.setAddressee(userRepo.getReferenceById(addresseeId));
    friendship.setStatus(FriendshipStatus.PENDING);

    Friendship saved = friendshipRepo.save(friendship);
    notificationService.send(addresseeId, NotificationType.FRIEND_REQUEST, saved.getId());
    return saved;
  }

  public Friendship acceptRequest(Long friendshipId, Long currentUserId) throws AccessDeniedException {
    Friendship friendship = friendshipRepo.findById(friendshipId)
        .orElseThrow(() -> new EntityNotFoundException("Request not found"));

    // Only the addressee can accept
    if (!friendship.getAddressee().getId().equals(currentUserId))
      throw new AccessDeniedException("Not your request to accept");

    friendship.setStatus(FriendshipStatus.ACCEPTED);
    return friendshipRepo.save(friendship);
  }
}
