package uk.ac.rhul.cs3821.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookCommentRepository;
import uk.ac.rhul.cs3821.repository.CommentLikeRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CommentLikeService.
 */
@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

  @Mock
  private CommentLikeRepository commentLikeRepository;
  @Mock
  private BookCommentRepository commentRepo;
  @Mock
  private UserRepository userRepo;

  @InjectMocks
  private CommentLikeService commentLikeService;

  private User user;
  private BookComment comment;

  /**
   * Sets up a test user and comment.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);

    comment = new BookComment();
    comment.setId(10L);
    comment.setUser(user);
    comment.setContent("Great book!");
  }

  @Test
  void toggleLike_addsLikeWhenNotAlreadyLiked() {
    when(commentLikeRepository.existsByUserIdAndCommentId(1L, 10L)).thenReturn(false);
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(commentRepo.findById(10L)).thenReturn(Optional.of(comment));
    when(commentLikeRepository.countByCommentId(10L)).thenReturn(1);

    int result = commentLikeService.toggleLike(1L, 10L);

    verify(commentLikeRepository).save(any());
    verify(commentLikeRepository, never()).deleteByUserIdAndCommentId(any(), any());
    assertEquals(1, result);
  }

  @Test
  void toggleLike_removesLikeWhenAlreadyLiked() {
    when(commentLikeRepository.existsByUserIdAndCommentId(1L, 10L)).thenReturn(true);
    when(commentLikeRepository.countByCommentId(10L)).thenReturn(0);

    int result = commentLikeService.toggleLike(1L, 10L);

    verify(commentLikeRepository).deleteByUserIdAndCommentId(1L, 10L);
    verify(commentLikeRepository, never()).save(any());
    assertEquals(0, result);
  }

  @Test
  void toggleLike_throwsWhenCommentNotFound() {
    when(commentLikeRepository.existsByUserIdAndCommentId(1L, 99L)).thenReturn(false);
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(commentRepo.findById(99L)).thenReturn(Optional.empty());

    assertThrows(jakarta.persistence.EntityNotFoundException.class,
        () -> commentLikeService.toggleLike(1L, 99L));

    verify(commentLikeRepository, never()).save(any());
  }

  @Test
  void toggleLike_returnsUpdatedCount() {
    when(commentLikeRepository.existsByUserIdAndCommentId(1L, 10L)).thenReturn(false);
    when(userRepo.getReferenceById(1L)).thenReturn(user);
    when(commentRepo.findById(10L)).thenReturn(Optional.of(comment));
    when(commentLikeRepository.countByCommentId(10L)).thenReturn(5);

    int result = commentLikeService.toggleLike(1L, 10L);

    assertEquals(5, result);
  }
}