package uk.ac.rhul.cs3821.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

  // REGISTER success
  @Test
  void testRegisterSuccess() throws Exception {
    when(repo.existsByEmail("test@example.com")).thenReturn(false);
    when(encoder.encode("pass")).thenReturn("hashed");

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@example.com","password":"pass"}
                """))
        .andExpect(status().isOk());

    verify(repo, times(1)).save(any());
  }

  // REGISTER email already exists
  @Test
  void testRegisterEmailTaken() throws Exception {
    when(repo.existsByEmail("test@example.com")).thenReturn(true);

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@example.com","password":"pass"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Email taken"));
  }

  // LOGIN success
  @Test
  void testLoginSuccess() throws Exception {
    Authentication mockAuth = mock(Authentication.class);

    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mockAuth);

    when(mockAuth.getName()).thenReturn("test@example.com");
    Collection<? extends GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("ROLE_USER"));

    when(mockAuth.getAuthorities())
        .thenReturn((Collection) authorities);


    when(jwt.generate(eq("test@example.com"), any(Map.class))).thenReturn("fake-jwt");

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@example.com","password":"pass"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("fake-jwt"));
  }
}
