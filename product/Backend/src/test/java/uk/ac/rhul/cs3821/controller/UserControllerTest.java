package uk.ac.rhul.cs3821.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setTotalPoints(150);
    user.setCurrentStreak(3);
    user.setLongestStreak(7);
    user.setLastReadDate(java.time.LocalDate.of(2025, 3, 10));

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
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
    ReadingSession session = new ReadingSession();
    session.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
    session.setEndTime(Instant.now());

    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of(session));

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStreak").value(3))
        .andExpect(jsonPath("$.longestStreak").value(7))
        .andExpect(jsonPath("$.lastReadDate").value("2025-03-10"))
        .andExpect(jsonPath("$.readDates").isArray());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_returnsEmptyReadDatesWhenNoSessions() throws Exception {
    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStreak").value(3))
        .andExpect(jsonPath("$.readDates").isEmpty());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_handlesNullLastReadDate() throws Exception {
    user.setLastReadDate(null);
    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastReadDate").doesNotExist());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getStreak_deduplicatesSessionsOnSameDay() throws Exception {
    Instant today = Instant.now();
    ReadingSession s1 = new ReadingSession();
    s1.setStartTime(today);
    s1.setEndTime(today.plus(30, ChronoUnit.MINUTES));

    ReadingSession s2 = new ReadingSession();
    s2.setStartTime(today.plus(1, ChronoUnit.HOURS));
    s2.setEndTime(today.plus(2, ChronoUnit.HOURS));

    when(readingSessionRepository.findByUserIdAndEndTimeIsNotNull(1L))
        .thenReturn(List.of(s1, s2));

    mockMvc.perform(get("/api/users/streak"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readDates.length()").value(1));
  }
}