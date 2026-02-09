package uk.ac.rhul.cs3821.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Recommendation;

/**
 * Repository for managing Recommendation entities.
 */

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

  /**
   * Method returns list of recommended books according to the userId.
   *
   * @param userId to represent the unique user.
   * @return a list of recommended books.
   */
  List<Recommendation> findByUserIdOrderByScoreDesc(Long userId);
}
