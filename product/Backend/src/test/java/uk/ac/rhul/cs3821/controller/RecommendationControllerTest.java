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
import uk.ac.rhul.cs3821.model.Recommendation;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.RecommendationRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

  @Mock
  private RecommendationRepository recommendationRepository;
  @Mock
  private BookRepository bookRepository;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private RecommendationController controller;

  @BeforeEach
  void auth() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsRecommendedBooks() {
    User user = new User();
    user.setId(1L);

    Recommendation r1 = new Recommendation();
    r1.setBookId(1L);
    Recommendation r2 = new Recommendation();
    r2.setBookId(2L);

    Book b1 = new Book();
    b1.setId(1L);
    Book b2 = new Book();
    b2.setId(2L);

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(user));
    when(recommendationRepository.findByUserIdOrderByScoreDesc(1L))
        .thenReturn(List.of(r1, r2));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(b1));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(b2));

    List<Book> result = controller.getRecommendations();

    assertEquals(List.of(b1, b2), result);
  }

  @Test
  void throwsIfUserMissing() {
    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, controller::getRecommendations);
  }
}
