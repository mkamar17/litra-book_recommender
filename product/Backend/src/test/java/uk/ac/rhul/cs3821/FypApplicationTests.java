package uk.ac.rhul.cs3821;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.rhul.cs3821.config.LeaderboardScheduler;

@SpringBootTest
class FypApplicationTests {
  @MockitoBean
  LeaderboardScheduler leaderboardScheduler;

  @Test
  void contextLoads() {
  }

}
