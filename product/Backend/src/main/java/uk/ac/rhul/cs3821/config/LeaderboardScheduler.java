package uk.ac.rhul.cs3821.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.service.LeaderboardService;

/**
 * Scheduled job that periodically recomputes leaderboard cache entries.
 * Runs every 15 minutes to keep friend leaderboards up to date.
 */
@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

  private final LeaderboardService leaderboardService;

  /**
   * Recomputes leaderboard caches for all periods.
   * Triggered automatically every 15 minutes.
   */
  @Scheduled(fixedRate = 900000)
  public void refreshLeaderboards() {
    leaderboardService.recomputeAll(LeaderboardPeriod.WEEKLY);
    leaderboardService.recomputeAll(LeaderboardPeriod.MONTHLY);
    leaderboardService.recomputeAll(LeaderboardPeriod.ALL_TIME);
  }
}