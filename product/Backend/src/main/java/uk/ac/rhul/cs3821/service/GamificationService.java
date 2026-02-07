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

  // streak , badges, reading speed etc. can be scaled

}
