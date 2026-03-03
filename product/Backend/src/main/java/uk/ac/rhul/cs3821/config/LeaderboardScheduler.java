package uk.ac.rhul.cs3821.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.service.LeaderboardService;

@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

  private final LeaderboardService leaderboardService;

  // Runs every 15 minutes — recomputes weekly and monthly caches
  @Scheduled(fixedRate = 900000)
  public void refreshLeaderboards() {
    leaderboardService.recomputeAll(LeaderboardPeriod.WEEKLY);
    leaderboardService.recomputeAll(LeaderboardPeriod.MONTHLY);
    leaderboardService.recomputeAll(LeaderboardPeriod.ALL_TIME);
  }
}