//package uk.ac.rhul.cs3821.controller;
//
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import uk.ac.rhul.cs3821.dto.CommentDto;
//import uk.ac.rhul.cs3821.model.Book;
//import uk.ac.rhul.cs3821.model.BookComment;
//import uk.ac.rhul.cs3821.model.User;
//import uk.ac.rhul.cs3821.repository.UserRepository;
//import uk.ac.rhul.cs3821.service.CommentService;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CommentControllerTest {
//
//  @Mock
//  private CommentService commentService;
//  @Mock
//  private UserRepository userRepository;
//
//  @InjectMocks
//  private CommentController controller;
//
//  private User currentUser;
//  private Book book;
//  private CommentDto commentDto;
//
//  @BeforeEach
//  void setUp() {
//    currentUser = new User();
//    currentUser.setId(1L);
//    currentUser.setEmail("test@example.com");
//
//    book = new Book();
//    book.setId(1L);
//
//    commentDto = new CommentDto(1L, "test@example.com", "Great book!", null, List.of(), 0);
//
//    SecurityContextHolder.getContext()
//        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));
//  }
//
//  @AfterEach
//  void clear() {
//    SecurityContextHolder.clearContext();
//  }
//
//  private void mockCurrentUser() {
//    when(userRepository.findByEmail("test@example.com"))
//        .thenReturn(Optional.of(currentUser));
//  }
//
//  @Test
//  void getCommentsReturnsList() {
//    when(commentService.getComments(1L)).thenReturn(List.of(commentDto));
//
//    var response = controller.getComments(1L);
//
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(1, response.getBody().size());
//    assertEquals("Great book!", response.getBody().get(0).content);
//  }
//
//  @Test
//  void getCommentsReturnsEmptyList() {
//    when(commentService.getComments(1L)).thenReturn(List.of());
//
//    var response = controller.getComments(1L);
//
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(0, response.getBody().size());
//  }
//
//  @Test
//  void postCommentReturns201() {
//    mockCurrentUser();
//    BookComment comment = new BookComment();
//    comment.setContent("Loved it!");
//    comment.setUser(currentUser);
//    comment.setBook(book);
//
//    CommentDto dto = new CommentDto(2L, "test@example.com", "Loved it!", null, List.of(), 0);
//
//    when(commentService.addComment(1L, 1L, "Loved it!", null)).thenReturn(comment);
//    when(commentService.toDto(comment)).thenReturn(dto);
//
//    var request = new CommentController.CommentRequest("Loved it!", null);
//    var response = controller.postComment(1L, request);
//
//    assertEquals(HttpStatus.CREATED, response.getStatusCode());
//    assertEquals("Loved it!", response.getBody().content);
//  }
//
//  @Test
//  void postReplyReturns201() {
//    mockCurrentUser();
//    BookComment reply = new BookComment();
//    reply.setContent("I agree!");
//    reply.setUser(currentUser);
//    reply.setBook(book);
//
//    CommentDto replyDto = new CommentDto(3L, "test@example.com", "I agree!", null, List.of(), 0);
//
//    when(commentService.addComment(1L, 1L, "I agree!", 5L)).thenReturn(reply);
//    when(commentService.toDto(reply)).thenReturn(replyDto);
//
//    var request = new CommentController.CommentRequest("I agree!", 5L);
//    var response = controller.postComment(1L, request);
//
//    assertEquals(HttpStatus.CREATED, response.getStatusCode());
//    assertEquals("I agree!", response.getBody().content);
//  }
//
//  @Test
//  void deleteCommentReturns204() throws Exception {
//    mockCurrentUser();
//
//    var response = controller.deleteComment(10L);
//
//    verify(commentService).deleteComment(10L, 1L);
//    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//  }
//
//  @Test
//  void throwsIfUserNotFound() {
//    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
//
//    org.junit.jupiter.api.Assertions.assertThrows(
//        jakarta.persistence.EntityNotFoundException.class,
//        () -> controller.postComment(1L, new CommentController.CommentRequest("test", null))
//    );
//  }
//}