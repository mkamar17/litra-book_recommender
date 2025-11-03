package uk.ac.rhul.cs3821.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.ac.rhul.cs3821.service.JwtService;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService uds;

    public JwtFilter(JwtService jwt, UserDetailsService uds) {
        this.jwt = jwt;
        this.uds = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws java.io.IOException, ServletException {

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String email = jwt.getSubject(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = uds.loadUserByUsername(email);
                    var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(req, res);
    }
}
