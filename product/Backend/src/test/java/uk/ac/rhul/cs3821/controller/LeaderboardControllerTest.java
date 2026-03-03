package uk.ac.rhul.cs3821.controller;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.dto.LeaderboardEntryDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.LeaderboardService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

  @Mock
  private LeaderboardService leaderboardService;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private LeaderboardController controller;

  private User currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(currentUser));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void weeklyLeaderboardReturnsRankedFriends() {
    List<LeaderboardEntryDto> entries = List.of(
        new LeaderboardEntryDto("top@example.com", 500, 200, 3, "WEEKLY"),
        new LeaderboardEntryDto("mid@example.com", 300, 100, 1, "WEEKLY")
    );
    when(leaderboardService.getFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY))
        .thenReturn(entries);

    var response = controller.getFriendsLeaderboard(LeaderboardPeriod.WEEKLY);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
    assertEquals("top@example.com", response.getBody().get(0).email);
  }

  @Test
  void monthlyLeaderboardReturnsCorrectPeriod() {
    List<LeaderboardEntryDto> entries = List.of(
        new LeaderboardEntryDto("friend@example.com", 1200, 500, 8, "MONTHLY")
    );
    when(leaderboardService.getFriendsLeaderboard(1L, LeaderboardPeriod.MONTHLY))
        .thenReturn(entries);

    var response = controller.getFriendsLeaderboard(LeaderboardPeriod.MONTHLY);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("MONTHLY", response.getBody().get(0).period);
  }

  @Test
  void returnsEmptyListIfNoFriends() {
    when(leaderboardService.getFriendsLeaderboard(1L, LeaderboardPeriod.WEEKLY))
        .thenReturn(List.of());

    var response = controller.getFriendsLeaderboard(LeaderboardPeriod.WEEKLY);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void throwsIfUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        jakarta.persistence.EntityNotFoundException.class,
        () -> controller.getFriendsLeaderboard(LeaderboardPeriod.WEEKLY)
    );
  }
}