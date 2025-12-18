package uk.ac.rhul.cs3821.config;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

  private SecurityConfig securityConfig;

  @BeforeEach
  void setUp() {
    JwtFilter mockFilter = Mockito.mock(JwtFilter.class);
    AppUserDetailsService mockUds = Mockito.mock(AppUserDetailsService.class);

    securityConfig = new SecurityConfig(mockFilter, mockUds);
  }

  @Test
  void testCorsConfigurationSource() {
    CorsConfigurationSource source = securityConfig.corsConfigurationSource();
    assertNotNull(source, "CORS configuration source should not be null");

    MockHttpServletRequest request = new MockHttpServletRequest();
    CorsConfiguration config = source.getCorsConfiguration(request);

    assertNotNull(config, "CORS configuration should not be null");
    assertEquals(List.of("http://localhost:5173", "http://localhost:8080"), config.getAllowedOrigins());
    assertTrue(config.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
    assertTrue(config.getAllowedHeaders().containsAll(List.of("Authorization", "Content-Type")));
    assertTrue(config.getAllowCredentials());
  }
}
