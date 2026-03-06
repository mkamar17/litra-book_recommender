package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.LeaderboardCache;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;

/**
 * Repository for LeaderboardCache entities.
 * Provides queries for reading precomputed leaderboard rankings.
 */
public interface LeaderboardCacheRepository extends JpaRepository<LeaderboardCache, Long> {

  /**
   * Returns the cached leaderboard entry for a specific user and period, if it exists.
   *
   * @param userId the ID of the user
   * @param period the leaderboard period — WEEKLY, MONTHLY, or ALL_TIME
   * @return an Optional containing the cache entry if found
   */
  Optional<LeaderboardCache> findByUserIdAndPeriod(Long userId, LeaderboardPeriod period);

  /**
   * Returns leaderboard entries for all accepted friends of a user, ranked by points.
   * Resolves friends by checking both sides of the friendship relationship.
   *
   * @param userId the ID of the user whose friends' rankings to retrieve
   * @param period the leaderboard period to filter by
   * @return list of LeaderboardCache entries ordered by points descending
   */
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