package uk.ac.rhul.cs3821.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtFilterTest {

  @Mock
  JwtService jwt;
  @Mock
  AppUserDetailsService uds;
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  FilterChain chain;
  @Mock
  UserDetails userDetails;

  JwtFilter filter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new JwtFilter(jwt, uds);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // Should authenticate when token is valid
  @Test
  void shouldAuthenticateWithValidToken() throws ServletException, IOException {

    when(req.getHeader("Authorization")).thenReturn("Bearer abc123");
    when(jwt.extractUserName("abc123")).thenReturn("maryam");
    when(uds.loadUserByUsername("maryam")).thenReturn(userDetails);
    when(jwt.validateToken("abc123", userDetails)).thenReturn(true);
    when(userDetails.getAuthorities()).thenReturn(List.of());
    when(userDetails.getUsername()).thenReturn("maryam");

    filter.doFilterInternal(req, res, chain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("maryam", SecurityContextHolder.getContext()
        .getAuthentication().getName());

    verify(chain, times(1)).doFilter(req, res);
  }

  // Should NOT authenticate on missing Authorization header
  @Test
  void shouldSkipWhenNoAuthHeader() throws ServletException, IOException {

    when(req.getHeader("Authorization")).thenReturn(null);

    filter.doFilterInternal(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(req, res);
  }

  // Should skip when header is not Bearer
  @Test
  void shouldSkipWhenHeaderNotBearer() throws ServletException, IOException {

    when(req.getHeader("Authorization")).thenReturn("Basic 1234");

    filter.doFilterInternal(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(req, res);
  }

  // Should skip when username = null
  @Test
  void shouldSkipWhenNoUsername() throws ServletException, IOException {

    when(req.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwt.extractUserName("token")).thenReturn(null);

    filter.doFilterInternal(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(req, res);
  }

  // Should not re-authenticate if already authenticated
  @Test
  void shouldSkipWhenAlreadyAuthenticated() throws ServletException, IOException {

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("existing", null)
    );

    when(req.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwt.extractUserName("token")).thenReturn("maryam");

    filter.doFilterInternal(req, res, chain);

    // stays as "existing" authentication
    assertEquals("existing", SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal());

    verify(chain).doFilter(req, res);
  }

  // Should skip on invalid token
  @Test
  void shouldSkipWhenTokenInvalid() throws ServletException, IOException {

    when(req.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwt.extractUserName("token")).thenReturn("maryam");
    when(uds.loadUserByUsername("maryam")).thenReturn(userDetails);
    when(jwt.validateToken("token", userDetails)).thenReturn(false);

    filter.doFilterInternal(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(req, res);
  }
}
