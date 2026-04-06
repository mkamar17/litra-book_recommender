package uk.ac.rhul.cs3821.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;

/**
 * REST controller for managing book operations.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookRepository bookRepository;

  /**
   * Returns all books stored locally.
   *
   * @return list of books
   */
  @GetMapping
  public ResponseEntity<List<Book>> all() {
    return ResponseEntity.ok(bookRepository.findAll());
  }

  /**
   * Deletes a book by its identifier.
   *
   * @param id book identifier
   * @return success or error message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteBook(@PathVariable Long id) {
    if (!bookRepository.existsById(id)) {
      return ResponseEntity.status(404).body("Book not found.");
    }
    bookRepository.deleteById(id);
    return ResponseEntity.ok("Book deleted successfully.");
  }

  /**
   * Method returns books with specific genre.
   *
   * @param genre the name of the genre
   * @return list of books with the specified genre
   */
  @GetMapping("/genre/{genre}")
  public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable String genre) {
    return ResponseEntity.ok(bookRepository.findByGenreIgnoreCase(genre));
  }
}
