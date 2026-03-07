package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.config.LeaderboardScheduler;
import uk.ac.rhul.cs3821.dto.LeaderboardEntryDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.LeaderboardService;

/**
 * Controller retrieves leaderboard data.
 * Returns precomputed rankings from the leaderboard cache.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

  private final LeaderboardService leaderboardService;
  private final UserRepository userRepository;
  private final LeaderboardScheduler leaderboardScheduler;

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  /**
   * Returns the friends leaderboard for the current user.
   * Defaults to WEEKLY if no period is specified.
   *
   * @param period the leaderboard period — WEEKLY, MONTHLY, or ALL_TIME
   * @return ranked list of LeaderboardEntryDto for the user's friends
   */
  @GetMapping("/friends")
  public ResponseEntity<List<LeaderboardEntryDto>> getFriendsLeaderboard(
      @RequestParam(defaultValue = "WEEKLY") LeaderboardPeriod period) {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(leaderboardService.getFriendsLeaderboard(userId, period));
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refresh() {
    leaderboardScheduler.refreshLeaderboards();
    return ResponseEntity.noContent().build();
  }
}