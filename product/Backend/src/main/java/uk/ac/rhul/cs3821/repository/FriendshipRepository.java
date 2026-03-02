package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.Friendship;
import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  // Pending requests sent TO a user
  List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

  // All accepted friendships involving a user (needs @Query — two columns to check)
  @Query("""
          SELECT f FROM Friendship f
          WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
            AND f.status = 'ACCEPTED'
      """)
  List<Friendship> findAllAcceptedFriendships(@Param("userId") Long userId);

  // Check if relationship already exists before sending a request
  @Query("""
          SELECT f FROM Friendship f
          WHERE (f.requester.id = :userA AND f.addressee.id = :userB)
             OR (f.requester.id = :userB AND f.addressee.id = :userA)
      """)
  Optional<Friendship> findBetweenUsers(@Param("userA") Long userA,
                                        @Param("userB") Long userB);

  // Used in service layer to gate friend-only features
  @Query("""
          SELECT COUNT(f) > 0 FROM Friendship f
          WHERE (f.requester.id = :userA AND f.addressee.id = :userB
             OR  f.requester.id = :userB AND f.addressee.id = :userA)
            AND f.status = 'ACCEPTED'
      """)
  boolean areFriends(@Param("userA") Long userA, @Param("userB") Long userB);
}
