package uk.ac.rhul.cs3821.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;

/**
 * Security configuration for CORS and HTTP request authorisation.
 */
@Configuration
public class SecurityConfig {

  private final JwtFilter jwtFilter;
  private final AppUserDetailsService uds;

  /**
   * Creates the filter with required services.
   *
   * @param jwtFilter the JWT service used to validate tokens
   * @param uds       loads user details for authentication
   */
  public SecurityConfig(JwtFilter jwtFilter, AppUserDetailsService uds) {
    this.jwtFilter = jwtFilter;
    this.uds = uds;
  }

  /**
   * Configures the security filter chain.
   *
   * @param http the HTTP security builder
   * @return the configured security filter chain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> {
        })
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/api/books", "/api/books/**").permitAll()
            .requestMatchers("/api/library/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Creates a password encoder using BCrypt.
   *
   * @return a BCrypt-based password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Configures the authentication provider to use the custom user details
   * service and password encoder.
   *
   * @return the configured authentication provider
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(passwordEncoder());
    return p;
  }

  /**
   * Exposes the authentication manager bean used during authentication.
   *
   * @param cfg the authentication configuration
   * @return the authentication manager instance
   * @throws Exception if retrieval fails
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
      throws Exception {
    return cfg.getAuthenticationManager();
  }

  /**
   * Provides the CORS configuration source for the application.
   *
   * @return a configured CORS configuration source
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "http://localhost:8080"
    ));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    config.setAllowCredentials(true);

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
