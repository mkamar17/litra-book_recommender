package uk.ac.rhul.cs3821.service;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setup() throws Exception {
    jwtService = new JwtService();

    // setting secret key
    Field secretField = JwtService.class.getDeclaredField("secretKey");
    secretField.setAccessible(true);

    secretField.set(jwtService, "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9");
  }

  @Test
  void testGenerateAndExtractUsername() {
    String token = jwtService.generate("alice@example.com");

    String extracted = jwtService.extractUserName(token);

    assertEquals("alice@example.com", extracted);
  }

  @Test
  void testGenerateWithClaims() {
    String token = jwtService.generate("bob@example.com", Map.of("role", "ADMIN"));

    String extracted = jwtService.extractUserName(token);

    assertEquals("bob@example.com", extracted);
  }

  @Test
  void testValidateTokenSuccess() {
    String token = jwtService.generate("carol@example.com");

    UserDetails user = User.withUsername("carol@example.com")
        .password("pw")
        .roles("USER")
        .build();

    assertTrue(jwtService.validateToken(token, user));
  }

  @Test
  void testValidateTokenFailsWrongUser() {
    String token = jwtService.generate("dave@example.com");

    UserDetails user = User.withUsername("eve@example.com")
        .password("pw")
        .roles("USER")
        .build();

    assertFalse(jwtService.validateToken(token, user));
  }

  @Test
  void testTokenIsNotExpired() {
    String token = jwtService.generate("fay@example.com");

    assertFalse(tokenIsExpired(token));
  }

  /**
   * This helper method uses reflection to call private extractExpiration
   */
  private boolean tokenIsExpired(String token) {
    try {
      var m = JwtService.class.getDeclaredMethod("extractExpiration", String.class);
      m.setAccessible(true);
      Date exp = (Date) m.invoke(jwtService, token);
      return exp.before(new Date());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}