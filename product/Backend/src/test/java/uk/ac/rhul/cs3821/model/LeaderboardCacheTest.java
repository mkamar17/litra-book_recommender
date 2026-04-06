package uk.ac.rhul.cs3821.model;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LeaderboardCacheTest {

  private User user;
  private LeaderboardCache cache;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("reader@example.com");

    cache = new LeaderboardCache();
    cache.setUser(user);
    cache.setPeriod(LeaderboardPeriod.WEEKLY);
    cache.setPoints(250);
    cache.setPagesRead(100);
    cache.setBooksCompleted(2);
    cache.setComputedAt(LocalDateTime.of(2026, 3, 1, 12, 0));
  }

  @Test
  void settersAndGettersWork() {
    assertEquals(user, cache.getUser());
    assertEquals(LeaderboardPeriod.WEEKLY, cache.getPeriod());
    assertEquals(250, cache.getPoints());
    assertEquals(100, cache.getPagesRead());
    assertEquals(2, cache.getBooksCompleted());
    assertEquals(LocalDateTime.of(2026, 3, 1, 12, 0), cache.getComputedAt());
  }

  @Test
  void pointsCanBeUpdated() {
    cache.setPoints(500);
    assertEquals(500, cache.getPoints());
  }

  @Test
  void periodCanBeChangedToMonthly() {
    cache.setPeriod(LeaderboardPeriod.MONTHLY);
    assertEquals(LeaderboardPeriod.MONTHLY, cache.getPeriod());
  }

  @Test
  void periodCanBeChangedToAllTime() {
    cache.setPeriod(LeaderboardPeriod.ALL_TIME);
    assertEquals(LeaderboardPeriod.ALL_TIME, cache.getPeriod());
  }

  @Test
  void defaultPointsAreZero() {
    LeaderboardCache empty = new LeaderboardCache();
    assertEquals(0, empty.getPoints());
    assertEquals(0, empty.getPagesRead());
    assertEquals(0, empty.getBooksCompleted());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    LeaderboardCache empty = new LeaderboardCache();
    assertNull(empty.getId());
    assertNull(empty.getUser());
    assertNull(empty.getPeriod());
  }
}