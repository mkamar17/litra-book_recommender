package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.rhul.cs3821.dto.RatingDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookRating;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRatingRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * REST controller for managing book ratings.
 * Handles retrieving, submitting, and deleting ratings for a specific book.
 */
@RestController
@RequestMapping("/api/books/{bookId}/rating")
@RequiredArgsConstructor
public class RatingController {

  private final BookRatingRepository ratingRepo;
  private final BookRepository bookRepo;
  private final UserRepository userRepo;
  private final WebClient recommenderClient = WebClient.builder()
      .baseUrl("http://localhost:5001")
      .build();

  /**
   * Resolves the currently authenticated user from the security context.
   *
   * @return the authenticated User
   * @throws EntityNotFoundException if the user is not found
   */

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  /**
   * Returns the current user's rating, average rating, and total rating count for a book.
   *
   * @param bookId the ID of the book
   * @return rating summary including userRating, avgRating, and ratingCount
   */
  @GetMapping
  public ResponseEntity<?> getRating(@PathVariable Long bookId) {
    User user = getCurrentUser();
    Optional<BookRating> existing = ratingRepo.findByUserIdAndBookId(user.getId(), bookId);
    double avg = ratingRepo.findAvgRatingByBookId(bookId).orElse(0.0);
    long count = ratingRepo.countByBookId(bookId);

    return ResponseEntity.ok(Map.of(
        "userRating", existing.map(BookRating::getRating).orElse((short) 0),
        "avgRating", Math.round(avg * 10.0) / 10.0,
        "ratingCount", count
    ));
  }

  /**
   * Returns the current user's rating, average rating, and total rating count for a book.
   *
   * @param bookId the ID of the book
   * @return rating summary including userRating, avgRating, and ratingCount
   */
  @PostMapping
  public ResponseEntity<?> submitRating(
      @PathVariable Long bookId,
      @RequestBody RatingDto dto) {

    if (dto.rating() < 1 || dto.rating() > 5) {
      return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
    }

    User user = getCurrentUser();
    Book book = bookRepo.findById(bookId).orElseThrow();

    BookRating rating = ratingRepo.findByUserIdAndBookId(user.getId(), bookId)
        .map(existing -> {
          existing.setRating(dto.rating());
          return existing;
        })
        .orElseGet(() -> BookRating.builder().user(user).book(book).rating(dto.rating()).build());

    ratingRepo.save(rating);

    double avg = ratingRepo.findAvgRatingByBookId(bookId).orElse(0.0);
    long count = ratingRepo.countByBookId(bookId);

    triggerRecommenderUpdate(user.getId());
    return ResponseEntity.ok(Map.of(
        "userRating", rating.getRating(),
        "avgRating", Math.round(avg * 10.0) / 10.0,
        "ratingCount", count
    ));
  }

  /**
   * Deletes the current user's rating for a book.
   * Triggers a recommender update after deletion.
   *
   * @param bookId the ID of the book
   * @return HTTP 204 on success
   */

  @DeleteMapping
  public ResponseEntity<?> deleteRating(@PathVariable Long bookId) {
    User user = getCurrentUser();
    ratingRepo.findByUserIdAndBookId(user.getId(), bookId)
        .ifPresent(ratingRepo::delete);

    triggerRecommenderUpdate(user.getId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Asynchronously triggers the Flask recommender service to refresh recommendations for a user.
   *
   * @param userId the ID of the user to update
   */
  
  private void triggerRecommenderUpdate(Long userId) {
    try {
      recommenderClient.post()
          .uri("/recommend/" + userId)
          .retrieve()
          .bodyToMono(String.class)
          .subscribe(
              res -> System.out.println("Recommender updated for user " + userId + ": " + res),
              err -> System.err.println("Recommender call failed: " + err.getMessage())
          );
    } catch (Exception e) {
      System.err.println("Could not reach recommender: " + e.getMessage());
    }
  }
}