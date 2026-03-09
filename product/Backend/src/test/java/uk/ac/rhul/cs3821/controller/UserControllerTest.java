package uk.ac.rhul.cs3821.controller;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
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

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setTotalPoints(150);

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
}
