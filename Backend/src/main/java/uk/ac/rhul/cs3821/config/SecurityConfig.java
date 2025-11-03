package uk.ac.rhul.cs3821.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

/**
 * Security configuration for CORS and HTTP request authorisation.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * @param http the HTTP security builder
     * @return the configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return httpSecurity
            .formLogin(httpForm -> {
                .loginPage("/login").permitAll();
            })

            .authorizeHttpRequests(registry -> {
                registry.requestMatchers("/req/signup").permitAll();
                registry.anyRequest().authenticated();
            })

            .build();
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
            "http://localhost:5173"
            // TODO: Add production domain later
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
