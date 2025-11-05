package uk.ac.rhul.cs3821.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller currently acts as a protected sample endpoint.
 */

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
  /**
   * Get method returns static list of books.
   *
   * @param auth to authenticate user.
   * @return static book list.
   */
  @GetMapping
  public List<String> get(Authentication auth) {
    return List.of("Fourth Wing", "The Women", "Where the Crawdads Sing");
  }
}