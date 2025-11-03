package uk.ac.rhul.cs3821.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller currently acts as a protected sample endpoint.
 *
 * @return static list of books
 *
 */
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    @GetMapping
    public List<String> get(Authentication auth) {
        // demo: use auth.getName() to personalise
        return List.of("Fourth Wing", "The Women", "Where the Crawdads Sing");
    }
}