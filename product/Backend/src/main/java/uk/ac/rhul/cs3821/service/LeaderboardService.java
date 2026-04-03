package uk.ac.rhul.cs3821.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.rhul.cs3821.dto.LeaderboardEntryDto;
import uk.ac.rhul.cs3821.model.LeaderboardCache;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.repository.LeaderboardCacheRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Service manages Leaderboard logic.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LeaderboardService {

  private final LeaderboardCacheRepository leaderboardCacheRepository;
  private final UserRepository userRepository;

  /**
   * Returns the friends leaderboard for the current user for a given period.
   */
  @Transactional(readOnly = true)
  public List<LeaderboardEntryDto> getFriendsLeaderboard(Long userId, LeaderboardPeriod period) {
    List<LeaderboardCache> friendEntries = leaderboardCacheRepository
        .findFriendsLeaderboard(userId, period);

    List<LeaderboardCache> all = new ArrayList<>(friendEntries);
    leaderboardCacheRepository.findByUserIdAndPeriod(userId, period)
        .ifPresent(all::add);

    return all.stream()
        .sorted(Comparator.comparingInt(LeaderboardCache::getPoints).reversed())
        .map(l -> new LeaderboardEntryDto(
            l.getUser().getUsername() != null
                ? l.getUser().getUsername()
                : l.getUser().getEmail(),
            l.getPoints(),
            l.getPagesRead(),
            l.getBooksCompleted(),
            l.getPeriod().name()))
        .toList();
  }

  /**
   * Recomputes leaderboard cache for all users for a given period.
   * Called by the scheduler — never called directly from a controller.
   */
  public void recomputeAll(LeaderboardPeriod period) {
    List<User> allUsers = userRepository.findAll();

    for (User user : allUsers) {
      LeaderboardCache cache = leaderboardCacheRepository
          .findByUserIdAndPeriod(user.getId(), period)
          .orElse(new LeaderboardCache());

      cache.setUser(user);
      cache.setPeriod(period);
      cache.setComputedAt(LocalDateTime.now());

      switch (period) {
        case WEEKLY -> {
          cache.setPoints(user.getWeeklyPoints());
          cache.setPagesRead(user.getWeeklyPagesRead());
          cache.setBooksCompleted(user.getWeeklyBooksCompleted());
        }
        case MONTHLY -> {
          cache.setPoints(user.getMonthlyPoints());
          cache.setPagesRead(user.getMonthlyPagesRead());
          cache.setBooksCompleted(user.getMonthlyBooksCompleted());
        }
        case ALL_TIME -> {
          cache.setPoints(user.getTotalPoints());
          cache.setPagesRead(user.getTotalPagesRead());
          cache.setBooksCompleted(user.getTotalBooksCompleted());
        }
        default -> throw new IllegalArgumentException("Unknown period: " + period);
      }

      leaderboardCacheRepository.save(cache);
    }
  }

  public void recomputeForUser(User user) {
    for (LeaderboardPeriod period : LeaderboardPeriod.values()) {
      LeaderboardCache cache = leaderboardCacheRepository
          .findByUserIdAndPeriod(user.getId(), period)
          .orElse(new LeaderboardCache());

      cache.setUser(user);
      cache.setPeriod(period);
      cache.setComputedAt(LocalDateTime.now());

      switch (period) {
        case WEEKLY -> {
          cache.setPoints(user.getWeeklyPoints());
          cache.setPagesRead(user.getWeeklyPagesRead());
          cache.setBooksCompleted(user.getWeeklyBooksCompleted());
        }
        case MONTHLY -> {
          cache.setPoints(user.getMonthlyPoints());
          cache.setPagesRead(user.getMonthlyPagesRead());
          cache.setBooksCompleted(user.getMonthlyBooksCompleted());
        }
        case ALL_TIME -> {
          cache.setPoints(user.getTotalPoints());
          cache.setPagesRead(user.getTotalPagesRead());
          cache.setBooksCompleted(user.getTotalBooksCompleted());
        }
      }

      leaderboardCacheRepository.save(cache);
    }
  }
}