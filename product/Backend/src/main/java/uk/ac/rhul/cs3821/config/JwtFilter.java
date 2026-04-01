package uk.ac.rhul.cs3821.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;

/**
 * A request filter that intercepts incoming HTTP requests once per request to
 * validate and process JSON Web Tokens (JWT) passed via the Authorization header.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final AppUserDetailsService uds;

  /**
   * Public constructor for JwtFilter.
   *
   * @param jwt the JWT service used for token parsing and validation
   * @param uds the user details service used to load authenticated users
   */
  public JwtFilter(JwtService jwt, AppUserDetailsService uds) {
    this.jwt = jwt;
    this.uds = uds;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    final String authHeader = req.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {

      final String token = authHeader.substring(7);
      final String username;

      try {
        username = jwt.extractUserName(token);
      } catch (JwtException e) {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        return;
      }

      // check the user isn't already authenticated
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        var userDetails = uds.loadUserByUsername(username);

        if (jwt.validateToken(token, userDetails)) {

          var authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(req)
          );

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    }

    // continue the filter chain
    chain.doFilter(req, res);
  }
}
