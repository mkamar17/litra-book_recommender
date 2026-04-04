package uk.ac.rhul.cs3821.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Service responsible for handling gamification logic such as points, reading streaks, and badge eligibility.
 */
@Service
@RequiredArgsConstructor
public class GamificationService {

  private final UserRepository userRepository;

  /**
   * Method used to calculate points based on pages read.
   *
   * @param user      to represent user
   * @param pagesRead the number of pages read in that reading session
   */
  public int awardPointsForSession(User user, int pagesRead, boolean bookCompleted) {
    int pointsEarned = pagesRead * 5;

    user.setTotalPoints(user.getTotalPoints() + pointsEarned);
    user.setWeeklyPoints(user.getWeeklyPoints() + pointsEarned);
    user.setMonthlyPoints(user.getMonthlyPoints() + pointsEarned);

    user.setTotalPagesRead(user.getTotalPagesRead() + pagesRead);
    user.setWeeklyPagesRead(user.getWeeklyPagesRead() + pagesRead);
    user.setMonthlyPagesRead(user.getMonthlyPagesRead() + pagesRead);

    if (bookCompleted) {
      user.setTotalBooksCompleted(user.getTotalBooksCompleted() + 1);
      user.setWeeklyBooksCompleted(user.getWeeklyBooksCompleted() + 1);
      user.setMonthlyBooksCompleted(user.getMonthlyBooksCompleted() + 1);
    }

    userRepository.save(user);

    return pointsEarned;
  }

  // streak , badges, reading speed etc. can be scaled

  /**
   * Handles reading streak updates - incremented if the user read today or yesterday (continuing streak).
   * Minimum of 5 minutes reading session for update to be valid.
   *
   * @param user            the user to update
   * @param durationSeconds the duration of the completed session in seconds
   * @return the updated streak count, or -1 if session was too short
   */

  public int updateStreak(User user, long durationSeconds) {

    // five minute minimum for valid session
//    if (durationSeconds < 300) {
//      return -1;
//    }

    LocalDate today = LocalDate.now();
    LocalDate lastRead = user.getLastReadDate();

    if (lastRead != null && lastRead.equals(today)) {
      // already read today so no need to increment
      return user.getCurrentStreak();
    }

    if (lastRead != null && lastRead.equals(today.minusDays(1))) {
      // this means the user has read yesterday, so continue the streak
      user.setCurrentStreak(user.getCurrentStreak() + 1);
    } else {
      // missed a day
      user.setCurrentStreak(1);
    }

    // if user beats their longest streak...
    if (user.getCurrentStreak() > user.getLongestStreak()) {
      user.setLongestStreak(user.getCurrentStreak());
    }

    user.setLastReadDate(today);

    List<LocalDate> readDates = user.getReadDates();
    if (!readDates.contains(today)) {
      readDates.add(today);
    }

    userRepository.save(user);

    return user.getCurrentStreak();
  }
}
