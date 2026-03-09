package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * REST controller for managing user-related endpoints.
 * Provides API endpoints for user profile, points, and statistics.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final ReadingSessionRepository readingSessionRepository;

  /**
   * Gets the total accumulated points for the authenticated user.
   *
   * @return the user's total points
   */
  @GetMapping("/total-points")
  public ResponseEntity<?> getTotalPoints() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(Map.of("totalPoints", user.getTotalPoints()));
  }

  /**
   * Returns the current user's streak data.
   *
   * @return response with current and longest streak and last read date and read dates for calendar
   */
  @GetMapping("/streak")
  public ResponseEntity<Map<String, Object>> getStreak() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    List<LocalDate> readDates = readingSessionRepository
        .findByUserIdAndEndTimeIsNotNull(user.getId())
        .stream()
        .map(s -> s.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate())
        .distinct()
        .toList();

    return ResponseEntity.ok(Map.of(
        "currentStreak", user.getCurrentStreak(),
        "longestStreak", user.getLongestStreak(),
        "lastReadDate", user.getLastReadDate() != null ? user.getLastReadDate().toString() : null,
        "readDates", readDates.stream().map(LocalDate::toString).toList()
    ));
  }

  // Future endpoints you might add:

  // @GetMapping("/profile")
  // public ResponseEntity<?> getProfile() { ... }

  // @GetMapping("/badges")
  // public ResponseEntity<?> getBadges() { ... }

  // @GetMapping("/reading-stats")
  // public ResponseEntity<?> getReadingStats() { ... }
}