package uk.ac.rhul.cs3821.service;

import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GamificationService gamificationService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setTotalPoints(100);
    user.setCurrentStreak(0);
    user.setLongestStreak(0);
  }

  // --- awardPointsForSession ---

  @Test
  void awardPointsForSession_calculatesAndSavesPoints() {
    int pointsEarned = gamificationService.awardPointsForSession(user, 10);

    assertEquals(50, pointsEarned);
    assertEquals(150, user.getTotalPoints());
    verify(userRepository).save(user);
  }

  @Test
  void awardPointsForSession_zeroPagesEarnsZeroPoints() {
    int pointsEarned = gamificationService.awardPointsForSession(user, 0);

    assertEquals(0, pointsEarned);
    assertEquals(100, user.getTotalPoints());
    verify(userRepository).save(user);
  }

  // --- updateStreak ---

  @Test
  void updateStreak_returnsMinus1IfSessionTooShort() {
    int result = gamificationService.updateStreak(user, 299);

    assertEquals(-1, result);
    verify(userRepository, never()).save(user);
  }

  @Test
  void updateStreak_exactlyAtMinimumDurationIsValid() {
    user.setLastReadDate(LocalDate.now().minusDays(1));
    user.setCurrentStreak(2);

    int result = gamificationService.updateStreak(user, 300);

    assertEquals(3, result);
    verify(userRepository).save(user);
  }

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
    assertEquals(10, user.getLongestStreak()); // not beaten
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_readYesterdayBeatsLongestStreak() {
    user.setLastReadDate(LocalDate.now().minusDays(1));
    user.setCurrentStreak(9);
    user.setLongestStreak(9);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(10, result);
    assertEquals(10, user.getLongestStreak()); // new record
    verify(userRepository).save(user);
  }

  @Test
  void updateStreak_missedDayResetsStreakToOne() {
    user.setLastReadDate(LocalDate.now().minusDays(3));
    user.setCurrentStreak(8);
    user.setLongestStreak(8);

    int result = gamificationService.updateStreak(user, 600);

    assertEquals(1, result);
    assertEquals(8, user.getLongestStreak()); // longest preserved
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
}