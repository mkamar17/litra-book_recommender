package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.UserBookProgress;

/**
 * Repository for managing UserBookProgress entities.
 */
public interface UserBookProgressRepository
    extends JpaRepository<UserBookProgress, Long> {

  /**
   * This method finds the book progress for a given user and book.
   *
   * @param user the user
   * @param book the book
   */

  Optional<UserBookProgress> findByUserAndBook(User user, Book book);

  /**
   * This method finds all book progress for a specific user.
   *
   * @param user the user
   * @return a list of started books
   */
  List<UserBookProgress> findByUser(User user);
}