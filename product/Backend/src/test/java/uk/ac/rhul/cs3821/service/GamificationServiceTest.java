package uk.ac.rhul.cs3821.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
  }

  @Test
  void awardPointsForSession_calculatesAndSavesPoints() {

    int pagesRead = 10;

    int pointsEarned =
        gamificationService.awardPointsForSession(user, pagesRead);

    // 10 pages * 5 points
    assertEquals(50, pointsEarned);

    // totalPoints updated correctly
    assertEquals(150, user.getTotalPoints());

    // user persisted
    verify(userRepository).save(user);
  }
}