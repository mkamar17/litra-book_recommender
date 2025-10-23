package uk.ac.rhul.cs3821.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsSecurityFilterChainBean() {
        var chains = context.getBeansOfType(SecurityFilterChain.class);
        assertFalse(chains.isEmpty(), "At least one SecurityFilterChain bean should be registered");
        assertTrue(chains.size() >= 1, "Should have one or more SecurityFilterChain beans");
    }
}
