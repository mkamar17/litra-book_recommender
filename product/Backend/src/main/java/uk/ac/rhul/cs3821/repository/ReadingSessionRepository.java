package uk.ac.rhul.cs3821.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;

/**
 * Repository for managing ReadingSession entities.
 * Includes querying for active (ongoing) reading sessions.
 */

public interface ReadingSessionRepository
    extends JpaRepository<ReadingSession, Long> {

  /**
   * This method finds the active reading session for a given user and book.
   * A session is considered active if its endTime null.
   *
   * @param user the user
   * @param book the book
   * @return the active reading session if one exists, otherwise empty
   */

  Optional<ReadingSession> findByUserAndBookAndEndTimeIsNull(User user, Book book);
}
