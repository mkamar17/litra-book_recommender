package uk.ac.rhul.cs3821.controller;

import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
   * @param bookId the id of the book to add.
   * @param email  the email address of the user adding their book to their library.
   * @return a ResponseEntity indicating success.
   */
  @PostMapping("/add/{bookId}")
  public ResponseEntity<?> addBookToLibrary(@PathVariable Long bookId, @RequestParam String email) {
    User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

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
   * @param email the user's email
   * @return the list of books
   */
  @GetMapping
  public ResponseEntity<?> getUserLibrary(@RequestParam String email) {
    User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(user.getLibrary());
  }
}
