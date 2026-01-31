package uk.ac.rhul.cs3821.controller;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.UserBookProgress;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserBookProgressRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.ReadingSessionService;


/**
 * REST controller for managing reading session endpoints.
 * Provides API endpoints for starting and ending reading sessions.
 */

@RestController
@RequestMapping("/api/reading-sessions")
@RequiredArgsConstructor
public class ReadingSessionController {

  private final ReadingSessionService readingSessionService;
  private final BookRepository bookRepo;
  private final UserRepository userRepository;
  private final UserBookProgressRepository progressRepo;

  /**
   * Starts a new reading session for the authenticated user and given book.
   *
   * @param bookId the ID of the book to start reading
   * @return the created reading session
   */

  @PostMapping("/start/{bookId}")
  public ResponseEntity<?> start(@PathVariable Long bookId) {

    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();

    String email = auth.getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Book book = bookRepo.findById(bookId)
        .orElseThrow(() -> new RuntimeException("Book not found"));

    Optional<UserBookProgress> progress =
        readingSessionService.getProgress(user, book);

    ReadingSession session =
        readingSessionService.startSession(user, book);

    return ResponseEntity.ok(Map.of(
        "sessionId", session.getId(),
        "hasProgress", progress.isPresent(),
        "currentPage", progress
            .map(UserBookProgress::getCurrentPage)
            .orElse(0)
    ));
  }


  /**
   * Ends an existing reading session.
   *
   * @param sessionId the ID of the reading session to end
   * @return the updated reading session
   */

  @PostMapping("/end/{sessionId}")
  public ReadingSession end(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal User user,
      @RequestParam int pageReached
  ) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();

    String email = auth.getName();

    user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return readingSessionService.endSession(sessionId, user, pageReached);
  }

  /**
   * Creates a new reading progress record for the authenticated user and a given book.
   * A new progress entry is created with the current page initialised to 0.
   *
   * @param bookId     the ID of the book for which progress is being created
   * @param user       the authenticated user
   * @param totalPages the total number of pages in the book
   */

  @PostMapping("/progress/{bookId}")
  public void createProgress(
      @PathVariable Long bookId,
      @AuthenticationPrincipal User user,
      @RequestParam int totalPages) {

    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();

    String email = auth.getName();

    user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Book book = bookRepo.findById(bookId).orElseThrow();

//    if (progressRepo.findByUserAndBook(user, book).isPresent()) {
//      return;
//    }

    Optional<UserBookProgress> existing = progressRepo.findByUserAndBook(user, book);

    if (existing.isPresent()) {
      System.out.println("Progress already exists! Current page: " + existing.get().getCurrentPage());
      return;
    }

    System.out.println("Creating new progress for book " + bookId + " with " + totalPages + " pages");

    progressRepo.save(UserBookProgress.builder()
        .user(user)
        .book(book)
        .totalPages(totalPages)
        .currentPage(0)
        .build());

    System.out.println("Progress saved successfully!");
  }

//  /**
//   * Fetches current progress on a book.
//   *
//   * @param bookId the unique book id.
//   * @return the current progress, if it exists.
//   */
//  @GetMapping("/progress/{bookId}")
//  public ResponseEntity<?> getProgress(@PathVariable Long bookId) {
//
//    Authentication auth =
//        SecurityContextHolder.getContext().getAuthentication();
//
//    String email = auth.getName();
//
//    User user = userRepository.findByEmail(email)
//        .orElseThrow(() -> new RuntimeException("User not found"));
//
//    Book book = bookRepo.findById(bookId)
//        .orElseThrow(() -> new RuntimeException("Book not found"));

//    return readingSessionService
//        .getProgress(user, book)
//        .map(progress -> ResponseEntity.ok(Map.of(
//            "current_page", progress.getCurrentPage(),
//            "total_pages", progress.getTotalPages()
//        )))
//        .orElse(ResponseEntity.noContent().build());
//    UserBookProgress progress = readingSessionService
//        .getProgress(user, book)
//        .orElse(null);
//
//    if (progress == null) {
//      return ResponseEntity.ok(Map.of(
//          "current_page", 0,
//          "total_pages", 0
//      ));
//    }
//
//    return ResponseEntity.ok(Map.of(
//        "current_page", progress.getCurrentPage(),
//        "total_pages", progress.getTotalPages()
//    ));
//  }

  @GetMapping("/progress/{bookId}")
  public ResponseEntity<?> getProgress(
      @PathVariable Long bookId,
      @AuthenticationPrincipal User user
  ) {
    Book book = bookRepo.findById(bookId)
        .orElseThrow(() -> new RuntimeException("Book not found"));

    return readingSessionService
        .getProgress(user, book)
        .map(progress -> ResponseEntity.ok(Map.of(
            "current_page", progress.getCurrentPage(),
            "total_pages", progress.getTotalPages()
        )))
        .orElse(ResponseEntity.ok(Map.of(
            "current_page", 0,
            "total_pages", 0
        )));
  }


}
