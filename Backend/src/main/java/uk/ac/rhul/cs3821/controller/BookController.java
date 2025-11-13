package uk.ac.rhul.cs3821.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.service.BookService;

/**
 * REST controller for managing book operations.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService service;

  /**
   * Fetches books from Google Books API and stores them locally.
   *
   * @param max maximum number of books to fetch
   * @return list of persisted books
   */
  @PostMapping("/fetch")
  public ResponseEntity<List<Book>> fetch(@RequestParam(defaultValue = "20") int max) {
    return ResponseEntity.ok(service.fetchAndStorePopularFiction(max));
  }

  /**
   * Returns all books stored locally.
   *
   * @return list of books
   */
  @GetMapping
  public ResponseEntity<List<Book>> all() {
    return ResponseEntity.ok(service.getAll());
  }

  /**
   * Deletes a book by its identifier.
   *
   * @param id book identifier
   * @return success or error message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteBook(@PathVariable Long id) {
    final boolean deleted = service.deleteBookById(id);
    if (deleted) {
      return ResponseEntity.ok("Book deleted successfully.");
    } else {
      return ResponseEntity.status(404).body("Book not found.");
    }
  }

  /**
   * Method returns books with specific genre.
   *
   * @param genre the name of the genre
   * @return list of books with the specified genre
   */
  @GetMapping("/genre/{genre}")
  public List<Book> getBooksByGenre(@PathVariable String genre) {
    return service.getBooksByGenre(genre);
  }
}
