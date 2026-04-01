package uk.ac.rhul.cs3821.controller;

import java.security.Principal;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Controller responsible for managing users' My Library.
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

  private final UserRepository userRepo;
  private final BookRepository bookRepo;

  /**
   * Adds a specified book to users' My Library.
   *
   * <p>If the user or book cannot be found, an exception is thrown.</p>
   *
   * @param bookId    the id of the book to add.
   * @param principal to give the email
   * @return a ResponseEntity indicating success.
   */
  @PostMapping("/add/{bookId}")
  public ResponseEntity<?> addBookToLibrary(@PathVariable Long bookId, Principal principal) {
    User user = userRepo.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    Book book = bookRepo.findById(bookId)
        .orElseThrow(() -> new RuntimeException("Book not found"));

    if (user.getLibrary() == null) {
      user.setLibrary(new HashSet<>());
    }

    user.getLibrary().add(book);
    userRepo.save(user);

    return ResponseEntity.ok("Book added to library");
  }

  /**
   * Retrieves all the books saved in a user's library.
   *
   * @param principal to give the email
   * @return the list of books
   */
  @GetMapping
  public ResponseEntity<?> getUserLibrary(Principal principal) {
    User user = userRepo.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(user.getLibrary());
  }
}
