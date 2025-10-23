package uk.ac.rhul.cs3821.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source, "CORS configuration source should not be null");

        // ✅ Use a mock request instead of null
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config, "CORS configuration should not be null");
        assertEquals(List.of("http://localhost:5173"), config.getAllowedOrigins());
        assertTrue(config.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
        assertTrue(config.getAllowedHeaders().containsAll(List.of("Authorization", "Content-Type")));
        assertTrue(config.getAllowCredentials());
    }
}
