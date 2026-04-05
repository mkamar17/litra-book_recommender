package uk.ac.rhul.cs3821.service;

import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for GamificationService.
 */
@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GamificationService gamificationService;

  private User user;

  /**
   * Sets up a test user with base points and streaks.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setTotalPoints(100);
    user.setWeeklyPoints(0);
    user.setMonthlyPoints(0);
    user.setTotalPagesRead(0);
    user.setWeeklyPagesRead(0);
    user.setMonthlyPagesRead(0);
    user.setTotalBooksCompleted(0);
    user.setWeeklyBooksCompleted(0);
    user.setMonthlyBooksCompleted(0);
    user.setCurrentStreak(0);
    user.setLongestStreak(0);
    user.setReadDates(new ArrayList<>());
  }

  // --- awardPointsForSession ---

  @Test
  void awardPointsForSession_calculatesAndSavesPoints() {
    int pointsEarned = gamificationService.awardPointsForSession(user, 10, false);

    assertEquals(50, pointsEarned);
    assertEquals(150, user.getTotalPoints());
    assertEquals(50, user.getWeeklyPoints());
    assertEquals(10, user.getTotalPagesRead());
    verify(userRepository).save(user);
  }

  @Test
  void awardPointsForSession_zeroPagesEarnsZeroPoints() {
    int pointsEarned = gamificationService.awardPointsForSession(user, 0, false);

    assertEquals(0, pointsEarned);
    assertEquals(100, user.getTotalPoints());
    verify(userRepository).save(user);
  }

  @Test
  void awardPointsForSession_bookCompletedIncrementsCounters() {
    gamificationService.awardPointsForSession(user, 5, true);

    assertEquals(1, user.getTotalBooksCompleted());
    assertEquals(1, user.getWeeklyBooksCompleted());
    assertEquals(1, user.getMonthlyBooksCompleted());
    verify(userRepository).save(user);
  }

  @Test
  void awardPointsForSession_bookNotCompletedDoesNotIncrementCounters() {
    gamificationService.awardPointsForSession(user, 5, false);

    assertEquals(0, user.getTotalBooksCompleted());
    verify(userRepository).save(user);
  }

  // --- updateStreak ---

  @Test
  void updateStreak_alreadyReadTodayDoesNotIncrement() {
    user.setLastReadDate(LocalDate.now());
    user.setCurrentStreak(5);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(5, result);
    verify(userRepository, never()).save(user);
  }

  @Test
  void updateStreak_readYesterdayContinuesStreak() {
    user.setLastReadDate(LocalDate.now().minusDays(1));
    user.setCurrentStreak(4);
    user.setLongestStreak(10);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(5, result);
    assertEquals(10, user.getLongestStreak());
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_readYesterdayBeatsLongestStreak() {
    user.setLastReadDate(LocalDate.now().minusDays(1));
    user.setCurrentStreak(9);
    user.setLongestStreak(9);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(10, result);
    assertEquals(10, user.getLongestStreak());
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_missedDayResetsStreakToOne() {
    user.setLastReadDate(LocalDate.now().minusDays(3));
    user.setCurrentStreak(8);
    user.setLongestStreak(8);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(1, result);
    assertEquals(8, user.getLongestStreak());
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_nullLastReadDateStartsFreshStreak() {
    user.setLastReadDate(null);
    user.setCurrentStreak(0);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(1, result);
    assertEquals(LocalDate.now(), user.getLastReadDate());
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_setsLastReadDateToToday() {
    user.setLastReadDate(LocalDate.now().minusDays(1));

    gamificationService.updateStreak(user, 600);

    assertEquals(LocalDate.now(), user.getLastReadDate());
  }

  @Test
  void updateStreak_addsReadDateToList() {
    user.setLastReadDate(LocalDate.now().minusDays(1));

    gamificationService.updateStreak(user, 600);

    assertEquals(1, user.getReadDates().size());
    assertEquals(LocalDate.now(), user.getReadDates().get(0));
  }

  @Test
  void updateStreak_doesNotDuplicateReadDate() {
    user.setLastReadDate(LocalDate.now().minusDays(1));
    user.getReadDates().add(LocalDate.now());

    gamificationService.updateStreak(user, 600);

    assertEquals(1, user.getReadDates().size());
  }
}