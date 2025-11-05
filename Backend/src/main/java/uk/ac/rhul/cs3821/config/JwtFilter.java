package uk.ac.rhul.cs3821.config;

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

@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final AppUserDetailsService uds;

  public JwtFilter(JwtService jwt, AppUserDetailsService uds) {
    this.jwt = jwt;
    this.uds = uds;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    final String authHeader = req.getHeader("Authorization");

    // 1. Check "Authorization: Bearer ..."
    if (authHeader != null && authHeader.startsWith("Bearer ")) {

      final String token = authHeader.substring(7);
      final String username = jwt.extractUserName(token);

      // 2. Must not already be authenticated
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        var userDetails = uds.loadUserByUsername(username);

        // 3. Validate token
        if (jwt.validateToken(token, userDetails)) {

          var authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(req)
          );

          // 4. Set auth context
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    }

    // 5. Continue filter chain
    chain.doFilter(req, res);
  }
}
