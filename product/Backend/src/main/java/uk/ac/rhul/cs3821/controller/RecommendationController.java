package uk.ac.rhul.cs3821.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.Recommendation;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.RecommendationRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Controller for fetching ML-generated book recommendations.
 */

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final RecommendationRepository recommendationRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;

  /**
   * Get personalized book recommendations for a user.
   *
   * @return list of recommended books ordered by score (highest first)
   */
  @GetMapping
  public List<Book> getRecommendations() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // recommendations for this user, ordered by score (highest first)
    List<Recommendation> recommendations =
        recommendationRepository.findByUserIdOrderByScoreDesc(user.getId());

    //List<Long> bookIds = recommendations.stream()
    // .map(Recommendation::getBookId)
    // .collect(Collectors.toList());

    return recommendations.stream()
        .map(rec -> bookRepository.findById(rec.getBookId()).orElse(null))
        .filter(book -> book != null)
        .collect(Collectors.toList());

    //return bookRepository.findAllById(bookIds);
  }
}