package uk.ac.rhul.cs3821.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    // Fetch from Google Books and store
    @PostMapping("/fetch")
    public ResponseEntity<List<Book>> fetch(@RequestParam(defaultValue = "20") int max) {
        return ResponseEntity.ok(service.fetchAndStorePopularFiction(max));
    }

    // Read from DB for frontend
    @GetMapping
    public ResponseEntity<List<Book>> all() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        boolean deleted = service.deleteBookById(id);
        if (deleted) {
            return ResponseEntity.ok("Book deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Book not found.");
        }
    }


}