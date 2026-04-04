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
import uk.ac.rhul.cs3821.dto.RatingDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookRating;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRatingRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

@RestController
@RequestMapping("/api/books/{bookId}/rating")
@RequiredArgsConstructor
public class RatingController {

  private final BookRatingRepository ratingRepo;
  private final BookRepository bookRepo;
  private final UserRepository userRepo;

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

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

  @PostMapping
  public ResponseEntity<?> submitRating(
      @PathVariable Long bookId,
      @RequestBody RatingDto dto) {

    if (dto.rating() < 1 || dto.rating() > 5)
      return ResponseEntity.badRequest().body("Rating must be between 1 and 5");

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

    return ResponseEntity.ok(Map.of(
        "userRating", rating.getRating(),
        "avgRating", Math.round(avg * 10.0) / 10.0,
        "ratingCount", count
    ));
  }

  @DeleteMapping
  public ResponseEntity<?> deleteRating(@PathVariable Long bookId) {
    User user = getCurrentUser();
    ratingRepo.findByUserIdAndBookId(user.getId(), bookId)
        .ifPresent(ratingRepo::delete);
    return ResponseEntity.noContent().build();
  }
}