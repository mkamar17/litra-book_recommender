package uk.ac.rhul.cs3821.service;

import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.UserBookProgress;

/**
 * Service responsible for handling gamification logic such as points, reading streaks, and badge eligibility.
 */
@Service
public class GamificationService {

  private final ReadingSessionService readingSessionService;

  /**
   * Creates a new GamificationService.
   *
   * @param readingSessionService service providing reading statistics
   */
  public GamificationService(ReadingSessionService readingSessionService) {
    this.readingSessionService = readingSessionService;
  }

  /**
   * Calculates the total points earned by a user.
   *
   * @param user the user whose points are calculated
   * @return total points earned
   */
  public int calculateUserPoints(User user, Book book) {
    UserBookProgress progress = readingSessionService
        .getProgress(user, book)
        .orElseThrow(() ->
            new IllegalStateException("User progress not found"));

    int pagesRead = progress.getCurrentPage();
    return pagesRead * 5;
  }

  public void awardPointsForSession(User user, int pagesRead) {
    int points = pagesRead * 5;
    // store points, emit event, or just return value
  }

  // streak , badges, reading speed etc. can be scaled

}
