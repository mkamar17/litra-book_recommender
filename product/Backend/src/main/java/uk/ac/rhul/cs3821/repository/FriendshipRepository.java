package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.Friendship;
import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;

/**
 * Repository for Friendship entities.
 * Provides queries for managing and checking friendship relationships between users.
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  /**
   * Returns all friendships with a given status sent to a specific user.
   * Used to retrieve incoming pending friend requests.
   *
   * @param addresseeId the ID of the user receiving the requests
   * @param status      the friendship status to filter by
   * @return list of matching Friendship entities
   */
  List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

  /**
   * Returns all accepted friendships involving a user, regardless of
   * whether they were the requester or the addressee.
   *
   * @param userId the ID of the user
   * @return list of accepted Friendship entities
   */
  @Query("""
          SELECT f FROM Friendship f
          WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
            AND f.status = 'ACCEPTED'
      """)
  List<Friendship> findAllAcceptedFriendships(@Param("userId") Long userId);

  /**
   * Returns the friendship between two users if one exists, regardless of direction.
   * Used to prevent duplicate friend requests.
   *
   * @param userA the ID of one user
   * @param userB the ID of the other user
   * @return an Optional containing the friendship if found
   */
  @Query("""
          SELECT f FROM Friendship f
          WHERE (f.requester.id = :userA AND f.addressee.id = :userB)
             OR (f.requester.id = :userB AND f.addressee.id = :userA)
      """)
  Optional<Friendship> findBetweenUsers(@Param("userA") Long userA,
                                        @Param("userB") Long userB);

  /**
   * Returns whether two users have an accepted friendship.
   * Used throughout the service layer to gate friend-only features.
   *
   * @param userA the ID of one user
   * @param userB the ID of the other user
   * @return true if an accepted friendship exists, false otherwise
   */
  @Query("""
          SELECT COUNT(f) > 0 FROM Friendship f
          WHERE (f.requester.id = :userA AND f.addressee.id = :userB
             OR  f.requester.id = :userB AND f.addressee.id = :userA)
            AND f.status = 'ACCEPTED'
      """)
  boolean areFriends(@Param("userA") Long userA, @Param("userB") Long userB);
}