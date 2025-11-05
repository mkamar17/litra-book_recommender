package uk.ac.rhul.cs3821.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * This is a service class for JWT.
 */
@Service
public class JwtService {

  // 24h expiry
  private static final long EXPIRATION = 1000L * 60 * 60 * 24;

  @Value("${jwt.secret}")
  private String secretKey;

  /**
   * Generate a JWT with optional claims (roles, etc.).
   */
  public String generate(String username, Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)
        .subject(username)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .signWith(getKey())
        .compact();
  }

  /**
   * Simple token (no custom claims).
   */
  public String generate(String username) {
    return generate(username, new HashMap<>());
  }

  /**
   * Extract the username (subject) field from a token.
   */
  public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Generic claim extractor.
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Parse token + return claims.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Validate token matches user + not expired.
   */
  public boolean validateToken(String token, UserDetails userDetails) {
    final String userName = extractUserName(token);
    return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Decode Base64 secret into HMAC key.
   */
  private SecretKey getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

}
