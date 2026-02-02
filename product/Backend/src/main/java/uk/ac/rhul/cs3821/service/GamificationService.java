package uk.ac.rhul.cs3821.service;

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

  // private int pointsEarned;

//  /**
//   * Creates a new GamificationService.
//   *
//   * @param readingSessionService service providing reading statistics
//   */
//  public GamificationService(ReadingSessionService readingSessionService) {
//    this.readingSessionService = readingSessionService;
//  }

//  /**
//   * Calculates the total points earned by a user.
//   *
//   * @param user the user whose points are calculated
//   * @return total points earned
//   */
//  public int calculateUserPoints(User user, Book book) {
//    UserBookProgress progress = readingSessionService
//        .getProgress(user, book)
//        .orElseThrow(() ->
//            new IllegalStateException("User progress not found"));
//
//    int pagesRead = progress.getCurrentPage();
//    return pagesRead * 5;
//  }

  /**
   * Method used to calculate points based on pages read.
   *
   * @param user      to represent user
   * @param pagesRead the number of pages read in that reading session
   */
  public int awardPointsForSession(User user, int pagesRead) {
    int pointsEarned = pagesRead * 5;

    user.setTotalPoints(user.getTotalPoints() + pointsEarned);
    userRepository.save(user);

    return pointsEarned;
  }

//  public int getPointsForSession() {
//    return this.pointsEarned;
//  }

  // streak , badges, reading speed etc. can be scaled

}
