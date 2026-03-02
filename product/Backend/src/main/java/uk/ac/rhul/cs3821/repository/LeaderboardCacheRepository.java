package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.LeaderboardCache;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;

public interface LeaderboardCacheRepository extends JpaRepository<LeaderboardCache, Long> {

  Optional<LeaderboardCache> findByUserIdAndPeriod(Long userId, LeaderboardPeriod period);

  // Friends leaderboard — needs @Query because of the friendship join
  @Query("""
          SELECT l FROM LeaderboardCache l
          WHERE l.period = :period
            AND l.user.id IN (
                SELECT CASE
                  WHEN f.requester.id = :userId THEN f.addressee.id
                  ELSE f.requester.id
                END
                FROM Friendship f
                WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
                  AND f.status = 'ACCEPTED'
            )
          ORDER BY l.points DESC
      """)
  List<LeaderboardCache> findFriendsLeaderboard(@Param("userId") Long userId,
                                                @Param("period") LeaderboardPeriod period);
}