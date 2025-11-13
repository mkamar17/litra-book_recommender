package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Book;

/**
 * Repository for performing CRUD operations on Book entities.
 * Provides additional query helpers for looking up books by their external ID.
 */
public interface BookRepository extends JpaRepository<Book, Long> {
  /**
   * Finds a book by its external identifier.
   *
   * @param externalId the external Google Books volume ID
   * @return an Optional containing the matching book, if found
   */
  Optional<Book> findByExternalId(String externalId);

  /**
   * Finds books by genre.
   *
   * @param genre the genre name
   * @return a list of matching Book entities
   */

  List<Book> findByGenreIgnoreCase(String genre);
}