package uk.ac.rhul.cs3821.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.GamificationService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for UserController.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserRepository userRepository;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private AppUserDetailsService uds;
  @MockitoBean
  private ReadingSessionRepository readingSessionRepository;
  @MockitoBean
  private GamificationService gamificationService;

  private User user;

  /**
   * Sets up a test user with points and stub repository lookups.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setTotalPoints(150);

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
  }

  /**
   * Builds an Instant that falls on the given LocalDate at noon local time.
   */
  private Instant atNoon(LocalDate date) {
    return date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant();
  }

  private ReadingSession sessionOn(LocalDate date) {
    ReadingSession s = new ReadingSession();
    s.setStartTime(atNoon(date));
    s.setEndTime(atNoon(date).plus(30, ChronoUnit.MINUTES));
    return s;
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getTotalPoints_returnsUserPoints() throws Exception {
    mockMvc.perform(get("/api/users/total-points"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPoints").value(150));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_returnsStreakData() throws Exception {
    // Three consecutive days ending today → currentStreak = 3, longestStreak = 3
    LocalDate today = LocalDate.now();
    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of(
            sessionOn(today.minusDays(2)),
            sessionOn(today.minusDays(1)),
            sessionOn(today)
        ));

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStreak").value(3))
        .andExpect(jsonPath("$.longestStreak").value(3))
        .andExpect(jsonPath("$.readDates").isArray());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_returnsEmptyReadDatesWhenNoSessions() throws Exception {
    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStreak").value(0))
        .andExpect(jsonPath("$.readDates").isEmpty());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_handlesNullLastReadDate() throws Exception {
    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastReadDate").doesNotExist());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_deduplicatesSessionsOnSameDay() throws Exception {
    LocalDate today = LocalDate.now();
    Instant noon = atNoon(today);

    ReadingSession s1 = new ReadingSession();
    s1.setStartTime(noon);
    s1.setEndTime(noon.plus(30, ChronoUnit.MINUTES));

    ReadingSession s2 = new ReadingSession();
    s2.setStartTime(noon.plus(1, ChronoUnit.HOURS));
    s2.setEndTime(noon.plus(2, ChronoUnit.HOURS));

    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of(s1, s2));

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readDates.length()").value(1));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void updateUsername_savesAndReturnsUsername() throws Exception {
    mockMvc.perform(put("/api/users/username")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\": \"bookworm99\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("bookworm99"));
  }
}