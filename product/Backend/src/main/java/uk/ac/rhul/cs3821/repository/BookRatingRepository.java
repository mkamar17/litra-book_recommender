package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.BookRating;

/**
 * Repository for managing book ratings.
 */
public interface BookRatingRepository extends JpaRepository<BookRating, Long> {

  /**
   * Finds a user's rating for a specific book.
   *
   * @param userId the ID of the user
   * @param bookId the ID of the book
   * @return an Optional containing the rating if found
   */
  Optional<BookRating> findByUserIdAndBookId(Long userId, Long bookId);

  /**
   * Returns the average rating for a book.
   *
   * @param bookId the ID of the book
   * @return an Optional containing the average rating, or empty if no ratings exist
   */
  @Query("SELECT AVG(r.rating) FROM BookRating r WHERE r.book.id = :bookId")
  Optional<Double> findAvgRatingByBookId(@Param("bookId") Long bookId);

  /**
   * Returns the total number of ratings for a book.
   *
   * @param bookId the ID of the book
   * @return the rating count
   */
  long countByBookId(Long bookId);

  /**
   * Returns the total number of ratings submitted by a user.
   *
   * @param userId the ID of the user
   * @return the rating count
   */
  long countByUserId(Long userId);

  /**
   * Returns all ratings submitted by a user.
   *
   * @param userId the ID of the user
   * @return list of BookRating
   */
  List<BookRating> findByUserId(Long userId);
}