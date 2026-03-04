package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.ac.rhul.cs3821.dto.CommentDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.BookCommentRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.CommentLikeRepository;
import uk.ac.rhul.cs3821.repository.FriendshipRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private BookCommentRepository commentRepo;
  @Mock
  private BookRepository bookRepository;
  @Mock
  private UserRepository userRepo;
  @Mock
  private NotificationService notificationService;
  @Mock
  private CommentLikeRepository commentLikeRepository;
  @Mock
  private FriendshipRepository friendshipRepo;

  @InjectMocks
  private CommentService commentService;

  private User user;
  private Book book;
  private BookComment topLevelComment;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("reader@example.com");

    book = new Book();
    book.setId(1L);
    book.setTitle("1984");

    topLevelComment = new BookComment();
    topLevelComment.setId(5L);
    topLevelComment.setUser(user);
    topLevelComment.setBook(book);
    topLevelComment.setContent("Great read!");
    topLevelComment.setDeleted(false);
  }

  @Test
  void addComment_savesTopLevelComment() {
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(commentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    BookComment result = commentService.addComment(1L, 1L, "Great read!", null);

    assertEquals("Great read!", result.getContent());
    assertEquals(user, result.getUser());
    assertEquals(book, result.getBook());
    verify(commentRepo).save(any());
  }

  @Test
  void addComment_savesReplyAndNotifiesParentAuthor() {
    User otherUser = new User();
    otherUser.setId(2L);

    BookComment parentComment = new BookComment();
    parentComment.setId(5L);
    parentComment.setUser(otherUser);
    parentComment.setBook(book);
    parentComment.setContent("Original comment");

    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(commentRepo.findById(5L)).thenReturn(Optional.of(parentComment));
    when(commentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    BookComment result = commentService.addComment(1L, 1L, "I agree!", 5L);

    assertEquals(parentComment, result.getParentComment());
    verify(notificationService).send(2L, NotificationType.COMMENT_REPLY, 5L);
  }

  @Test
  void addComment_doesNotNotifyWhenReplyingToOwnComment() {
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(commentRepo.findById(5L)).thenReturn(Optional.of(topLevelComment));
    when(commentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    commentService.addComment(1L, 1L, "Adding more thoughts", 5L);

    verify(notificationService, never()).send(any(), any(), any());
  }

  @Test
  void addComment_throwsWhenBookNotFound() {
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
        () -> commentService.addComment(1L, 99L, "Test", null));
  }

  @Test
  void addComment_throwsWhenParentCommentNotFound() {
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(commentRepo.findById(99L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
        () -> commentService.addComment(1L, 1L, "Reply", 99L));
  }

  @Test
  void deleteComment_softDeletesOwnComment() {
    when(commentRepo.findById(5L)).thenReturn(Optional.of(topLevelComment));
    when(commentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    commentService.deleteComment(5L, 1L);

    assertTrue(topLevelComment.isDeleted());
    verify(commentRepo).save(topLevelComment);
  }

  @Test
  void deleteComment_throwsWhenNotOwner() {
    when(commentRepo.findById(5L)).thenReturn(Optional.of(topLevelComment));

    assertThrows(AccessDeniedException.class,
        () -> commentService.deleteComment(5L, 99L));

    verify(commentRepo, never()).save(any());
  }

  @Test
  void deleteComment_throwsWhenCommentNotFound() {
    when(commentRepo.findById(99L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
        () -> commentService.deleteComment(99L, 1L));
  }

  @Test
  void getComments_returnsListOfDtos() {
    when(commentRepo.findByBookIdAndParentCommentIsNullAndDeletedFalse(1L))
        .thenReturn(List.of(topLevelComment));
    when(commentLikeRepository.countByCommentId(5L)).thenReturn(0);

    List<CommentDto> result = commentService.getComments(1L);

    assertEquals(1, result.size());
    assertEquals("Great read!", result.getFirst().content);
    assertEquals("reader@example.com", result.getFirst().authorEmail);
    assertEquals(0, result.getFirst().likeCount);
  }

  @Test
  void toDto_includesLikeCount() {
    when(commentLikeRepository.countByCommentId(5L)).thenReturn(3);

    CommentDto dto = commentService.toDto(topLevelComment);

    assertEquals(3, dto.likeCount);
    assertEquals("Great read!", dto.content);
    assertEquals("reader@example.com", dto.authorEmail);
  }
}