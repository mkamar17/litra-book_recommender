package uk.ac.rhul.cs3821.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.service.ReadingSessionService;

/**
 * REST controller for managing reading session endpoints.
 * Provides API endpoints for starting and ending reading sessions.
 */

@RestController
@RequestMapping("/api/reading-sessions")
@RequiredArgsConstructor
public class ReadingSessionController {

  private final ReadingSessionService service;
  private final BookRepository bookRepo;

  /**
   * Starts a new reading session for the authenticated user and given book.
   *
   * @param bookId the ID of the book to start reading
   * @return the created reading session
   */

  @PostMapping("/start/{bookId}")
  public ReadingSession start(
      @PathVariable Long bookId,
      @AuthenticationPrincipal User user
  ) {
    Book book = bookRepo.findById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    ;
    return service.startSession(user, book);
  }

  /**
   * Ends an existing reading session.
   *
   * @param sessionId the ID of the reading session to end
   * @return the updated reading session
   */

  @PostMapping("/end/{sessionId}")
  public ReadingSession end(@PathVariable Long sessionId) {
    return service.endSession(sessionId);
  }
}
