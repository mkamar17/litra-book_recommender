package uk.ac.rhul.cs3821.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.rhul.cs3821.dto.LeaderboardEntryDto;
import uk.ac.rhul.cs3821.model.LeaderboardCache;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.repository.LeaderboardCacheRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

  @Mock
  private LeaderboardCacheRepository leaderboardCacheRepository;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private LeaderboardService leaderboardService;

  private User user;
  private User friend;
  private LeaderboardCache userCache;
  private LeaderboardCache friendCache;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("user@example.com");
    user.setTotalPoints(500);
    user.setWeeklyPoints(100);
    user.setMonthlyPoints(300);

    friend = new User();
    friend.setId(2L);
    friend.setEmail("friend@example.com");
    friend.setTotalPoints(800);
    friend.setWeeklyPoints(200);
    friend.setMonthlyPoints(600);

    userCache = new LeaderboardCache();
    userCache.setUser(user);
    userCache.setPeriod(LeaderboardPeriod.WEEKLY);
    userCache.setPoints(100);
    userCache.setPagesRead(40);
    userCache.setBooksCompleted(1);

    friendCache = new LeaderboardCache();
    friendCache.setUser(friend);
    friendCache.setPeriod(LeaderboardPeriod.WEEKLY);
    friendCache.setPoints(200);
    friendCache.setPagesRead(80);
    friendCache.setBooksCompleted(2);
  }

  @Test
  void getFriendsLeaderboard_returnsRankedEntries() {
    when(leaderboardCacheRepository.findFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY))
        .thenReturn(List.of(friendCache, userCache));

    List<LeaderboardEntryDto> result =
        leaderboardService.getFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY);

    assertEquals(2, result.size());
    assertEquals("friend@example.com", result.get(0).email());
    assertEquals(200, result.get(0).points());
    assertEquals("WEEKLY", result.get(0).period());
  }

  @Test
  void getFriendsLeaderboard_returnsEmptyWhenNoFriends() {
    when(leaderboardCacheRepository.findFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY))
        .thenReturn(List.of());

    List<LeaderboardEntryDto> result =
        leaderboardService.getFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY);

    assertEquals(0, result.size());
  }

  @Test
  void recomputeAll_createsNewCacheForEachUser() {
    when(userRepository.findAll()).thenReturn(List.of(user, friend));
    when(leaderboardCacheRepository.findByUserIdAndPeriod(any(), any()))
        .thenReturn(Optional.empty());

    leaderboardService.recomputeAll(LeaderboardPeriod.WEEKLY);

    // once per user
    verify(leaderboardCacheRepository, times(2)).save(any());
  }

  @Test
  void recomputeAll_updatesExistingCacheEntry() {
    when(userRepository.findAll()).thenReturn(List.of(user));
    when(leaderboardCacheRepository.findByUserIdAndPeriod(1L, LeaderboardPeriod.WEEKLY))
        .thenReturn(Optional.of(userCache));

    leaderboardService.recomputeAll(LeaderboardPeriod.WEEKLY);

    // weekly points from user (100) should be set on cache
    assertEquals(100, userCache.getPoints());
    verify(leaderboardCacheRepository).save(userCache);
  }

  @Test
  void recomputeAll_usesMonthlyPointsForMonthlyPeriod() {
    when(userRepository.findAll()).thenReturn(List.of(user));
    when(leaderboardCacheRepository.findByUserIdAndPeriod(1L, LeaderboardPeriod.MONTHLY))
        .thenReturn(Optional.of(userCache));

    leaderboardService.recomputeAll(LeaderboardPeriod.MONTHLY);

    assertEquals(300, userCache.getPoints());
  }

  @Test
  void recomputeAll_usesTotalPointsForAllTime() {
    when(userRepository.findAll()).thenReturn(List.of(user));
    when(leaderboardCacheRepository.findByUserIdAndPeriod(1L, LeaderboardPeriod.ALL_TIME))
        .thenReturn(Optional.of(userCache));

    leaderboardService.recomputeAll(LeaderboardPeriod.ALL_TIME);

    assertEquals(500, userCache.getPoints());
  }
}