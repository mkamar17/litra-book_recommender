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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.CommentService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

  @Mock
  private CommentService commentService;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CommentController controller;

  private User currentUser;
  private Book book;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");

    book = new Book();
    book.setId(1L);

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(currentUser));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getCommentsReturnsPagedResults() {
    BookComment comment = new BookComment();
    comment.setContent("Great book!");
    comment.setUser(currentUser);
    comment.setBook(book);

    Page<BookComment> page = new PageImpl<>(List.of(comment));
    when(commentService.getComments(1L, PageRequest.of(0, 20))).thenReturn(page);

    var response = controller.getComments(1L, 0, 20);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getContent().size());
  }

  @Test
  void postCommentReturns201() {
    BookComment comment = new BookComment();
    comment.setContent("Loved it!");
    comment.setUser(currentUser);
    comment.setBook(book);

    when(commentService.addComment(1L, 1L, "Loved it!", null)).thenReturn(comment);

    var request = new CommentController.CommentRequest("Loved it!", null);
    var response = controller.postComment(1L, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Loved it!", response.getBody().getContent());
  }

  @Test
  void postReplyCommentReturns201() {
    BookComment reply = new BookComment();
    reply.setContent("I agree!");
    reply.setUser(currentUser);
    reply.setBook(book);

    when(commentService.addComment(1L, 1L, "I agree!", 5L)).thenReturn(reply);

    var request = new CommentController.CommentRequest("I agree!", 5L);
    var response = controller.postComment(1L, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("I agree!", response.getBody().getContent());
  }

  @Test
  void deleteCommentReturns204() throws Exception {
    var response = controller.deleteComment(10L);

    verify(commentService).deleteComment(10L, 1L);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void throwsIfUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        jakarta.persistence.EntityNotFoundException.class,
        () -> controller.getComments(1L, 0, 20)
    );
  }
}