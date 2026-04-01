package uk.ac.rhul.cs3821.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_PASSWORD = "pass";
  private static final String LOGIN_JSON =
      "{\"email\":\"" + TEST_EMAIL + "\",\"password\":\"" + TEST_PASSWORD + "\"}";
  private static final String REGISTER_JSON = LOGIN_JSON;
  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private AuthenticationManager authManager;
  @MockitoBean
  private JwtService jwt;
  @MockitoBean
  private UserRepository repo;
  @MockitoBean
  private PasswordEncoder encoder;
  @MockitoBean
  private AppUserDetailsService uds;
  private Authentication mockAuth;

  @BeforeEach
  void setUp() {
    mockAuth = mock(Authentication.class);
    when(mockAuth.getName()).thenReturn(TEST_EMAIL);
    when(mockAuth.getAuthorities())
        .thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));
    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mockAuth);
    when(jwt.generate(eq(TEST_EMAIL), any(Map.class))).thenReturn("fake-jwt");
    when(encoder.encode(TEST_PASSWORD)).thenReturn("hashed");
  }

  @Test
  void testRegisterSuccess() throws Exception {
    when(repo.existsByEmail(TEST_EMAIL)).thenReturn(false);

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(REGISTER_JSON))
        .andExpect(status().isOk());

    verify(repo, times(1)).save(any());
  }

  @Test
  void testRegisterEmailTaken() throws Exception {
    when(repo.existsByEmail(TEST_EMAIL)).thenReturn(true);

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(REGISTER_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Registration failed. Please try again."));
  }

  @Test
  void testLoginSuccess() throws Exception {
    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(LOGIN_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("fake-jwt"));
  }

  @Test
  void testLoginRateLimitedAfterFiveAttempts() throws Exception {
    Mockito.reset(authManager);
    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    for (int i = 0; i < 5; i++) {
      mockMvc.perform(post("/auth/login")
              .with(req -> {
                req.setRemoteAddr("192.168.1.200");
                return req;
              })
              .contentType(MediaType.APPLICATION_JSON)
              .content(LOGIN_JSON))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(post("/auth/login")
            .with(req -> {
              req.setRemoteAddr("192.168.1.200");
              return req;
            })
            .contentType(MediaType.APPLICATION_JSON)
            .content(LOGIN_JSON))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().string("Too many login attempts. Please try again later."));
  }

  @Test
  void testLoginDifferentIpsNotRateLimited() throws Exception {
    for (int i = 1; i <= 6; i++) {
      final String ip = "10.0.1." + i;
      mockMvc.perform(post("/auth/login")
              .with(req -> {
                req.setRemoteAddr(ip);
                return req;
              })
              .contentType(MediaType.APPLICATION_JSON)
              .content(LOGIN_JSON))
          .andExpect(status().isOk());
    }
  }
}