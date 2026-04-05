package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.rhul.cs3821.model.BookRating;

public interface BookRatingRepository extends JpaRepository<BookRating, Long> {

  Optional<BookRating> findByUserIdAndBookId(Long userId, Long bookId);

  @Query("SELECT AVG(r.rating) FROM BookRating r WHERE r.book.id = :bookId")
  Optional<Double> findAvgRatingByBookId(@Param("bookId") Long bookId);

  long countByBookId(Long bookId);

  long countByUserId(Long userId);

  List<BookRating> findByUserId(Long userId);
}