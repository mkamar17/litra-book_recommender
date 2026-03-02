package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationTest {

  @Test
  void builderSetsAllFields() {
    Recommendation recommendation = Recommendation.builder()
        .id(1L)
        .userId(2L)
        .bookId(3L)
        .score(0.95)
        .build();

    assertAll(
        () -> assertEquals(1L, recommendation.getId()),
        () -> assertEquals(2L, recommendation.getUserId()),
        () -> assertEquals(3L, recommendation.getBookId()),
        () -> assertEquals(0.95, recommendation.getScore())
    );
  }

  @Test
  void settersAndGettersWork() {
    Recommendation recommendation = new Recommendation();

    recommendation.setUserId(5L);
    recommendation.setBookId(6L);
    recommendation.setScore(0.7);

    assertAll(
        () -> assertEquals(5L, recommendation.getUserId()),
        () -> assertEquals(6L, recommendation.getBookId()),
        () -> assertEquals(0.7, recommendation.getScore())
    );
  }

  @Test
  void allArgsConstructorSetsFields() {
    Recommendation recommendation =
        new Recommendation(1L, 10L, 20L, 0.85);

    assertAll(
        () -> assertEquals(1L, recommendation.getId()),
        () -> assertEquals(10L, recommendation.getUserId()),
        () -> assertEquals(20L, recommendation.getBookId()),
        () -> assertEquals(0.85, recommendation.getScore())
    );
  }
}
