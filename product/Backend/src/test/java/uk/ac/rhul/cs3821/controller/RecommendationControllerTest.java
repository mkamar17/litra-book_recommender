package uk.ac.rhul.cs3821.controller;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookRating;
import uk.ac.rhul.cs3821.model.Recommendation;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRatingRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.RecommendationRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RecommendationController.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

  @Mock
  private RecommendationRepository recommendationRepository;
  @Mock
  private BookRepository bookRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BookRatingRepository ratingRepository;

  @InjectMocks
  private RecommendationController controller;

  private User user;
  private Book b1;
  private Book b2;

  /**
   * Sets up a test user and books, and populates the security context.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);

    b1 = new Book();
    b1.setId(1L);
    b2 = new Book();
    b2.setId(2L);

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("test@example.com", null));
  }

  /**
   * Clears the security context after each test.
   */
  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsRecommendedBooks() {
    Recommendation r1 = new Recommendation();
    r1.setBookId(1L);
    Recommendation r2 = new Recommendation();
    r2.setBookId(2L);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(recommendationRepository.findByUserIdOrderByScoreDesc(1L)).thenReturn(List.of(r1, r2));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(b1));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(b2));
    when(ratingRepository.findByUserId(1L)).thenReturn(List.of());

    List<Book> result = controller.getRecommendations();

    assertEquals(List.of(b1, b2), result);
  }

  @Test
  void excludesAlreadyRatedBooks() {
    Recommendation r1 = new Recommendation();
    r1.setBookId(1L);
    Recommendation r2 = new Recommendation();
    r2.setBookId(2L);

    BookRating ratedB1 = new BookRating();
    ratedB1.setBook(b1);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(recommendationRepository.findByUserIdOrderByScoreDesc(1L)).thenReturn(List.of(r1, r2));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(b1));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(b2));
    when(ratingRepository.findByUserId(1L)).thenReturn(List.of(ratedB1));

    List<Book> result = controller.getRecommendations();

    assertEquals(1, result.size());
    assertEquals(b2, result.get(0));
  }

  @Test
  void returnsFallbackBooksWhenNoRecommendations() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(recommendationRepository.findByUserIdOrderByScoreDesc(1L)).thenReturn(List.of());
    when(bookRepository.findAll()).thenReturn(List.of(b1, b2));

    List<Book> result = controller.getRecommendations();

    assertEquals(List.of(b1, b2), result);
  }

  @Test
  void returnsEmptyWhenAllRecommendedBooksAreRated() {
    Recommendation r1 = new Recommendation();
    r1.setBookId(1L);

    BookRating rated = new BookRating();
    rated.setBook(b1);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(recommendationRepository.findByUserIdOrderByScoreDesc(1L)).thenReturn(List.of(r1));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(b1));
    when(ratingRepository.findByUserId(1L)).thenReturn(List.of(rated));

    List<Book> result = controller.getRecommendations();

    assertTrue(result.isEmpty());
  }

  @Test
  void throwsIfUserMissing() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, controller::getRecommendations);
  }
}