package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.rhul.cs3821.dto.CommentDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.BookCommentRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.CommentLikeRepository;
import uk.ac.rhul.cs3821.repository.FriendshipRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Service manages book comments.
 * Handles creating, deleting, and retrieving comments and replies.
 */
@Service
@Transactional
public class CommentService {

  private final BookCommentRepository commentRepo;
  private final NotificationService notificationService;
  private final UserRepository userRepo;
  private final BookRepository bookRepository;
  private final CommentLikeRepository commentLikeRepository;
  // you might need friendship repo if you recieve notif that friend liked your comment

  /**
   * Constructs a CommentService with all required dependencies.
   *
   * @param commentRepo           repository for comment entities
   * @param friendshipRepo        repository for friendship entities
   * @param notificationService   service for sending notifications
   * @param userRepo              repository for user entities
   * @param bookRepository        repository for book entities
   * @param commentLikeRepository repository for comment like entities
   */
  public CommentService(BookCommentRepository commentRepo, FriendshipRepository friendshipRepo,
                        NotificationService notificationService, UserRepository userRepo, BookRepository bookRepository,
                        CommentLikeRepository commentLikeRepository) {
    this.commentRepo = commentRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
    this.bookRepository = bookRepository;
    this.commentLikeRepository = commentLikeRepository;
  }

  /**
   * Adds a comment or reply to a book. If parentCommentId is provided,
   * the comment is treated as a reply and the parent author is notified,
   * unless the user is replying to their own comment.
   *
   * @param userId          the ID of the user posting the comment
   * @param bookId          the ID of the book being commented on
   * @param content         the text content of the comment
   * @param parentCommentId optional ID of the parent comment for replies
   * @return the saved BookComment entity
   * @throws EntityNotFoundException if the book or parent comment is not found
   */
  public BookComment addComment(Long userId, Long bookId,
                                String content, Long parentCommentId) {
    BookComment comment = new BookComment();
    comment.setUser(userRepo.getReferenceById(userId));

    Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("Book not found"));
    comment.setBook(book);

    comment.setContent(content);

    if (parentCommentId != null) {
      BookComment parent = commentRepo.findById(parentCommentId)
          .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
      comment.setParentComment(parent);

      // notify the original commenter of the reply
      if (!parent.getUser().getId().equals(userId)) {
        notificationService.send(parent.getUser().getId(),
            NotificationType.COMMENT_REPLY, parent.getId(), userId);
      }
    }

    return commentRepo.save(comment);
  }

  /**
   * Soft deletes a comment by marking it as deleted.
   * Only the comment author may delete their own comment.
   * The comment is retained in the database to preserve reply threads.
   *
   * @param commentId     the ID of the comment to delete
   * @param currentUserId the ID of the currently authenticated user
   * @throws EntityNotFoundException if the comment is not found
   * @throws AccessDeniedException   if the user is not the comment author
   */
  public void deleteComment(Long commentId, Long currentUserId) {
    BookComment comment = commentRepo.findById(commentId)
        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

    if (!comment.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Cannot delete another user's comment");
    }

    comment.setDeleted(true);  // soft delete — preserves reply threads
    commentRepo.save(comment);
  }

  /**
   * Returns all top-level comments for a book as DTOs, excluding deleted comments.
   *
   * @param bookId the ID of the book
   * @return list of CommentDto for the book
   */
  @Transactional(readOnly = true)
  public List<CommentDto> getComments(Long bookId) {
    return commentRepo.findByBookIdAndParentCommentIsNullAndDeletedFalse(bookId).stream().map(this::toDto).toList();
  }

  /**
   * Converts a BookComment entity to a CommentDto,
   * including the current like count.
   *
   * @param comment the comment entity to convert
   * @return the mapped CommentDto
   */
  @Transactional(readOnly = true)
  public CommentDto toDto(BookComment comment) {
    int likeCount = commentLikeRepository.countByCommentId(comment.getId());
    return CommentDto.from(comment, likeCount);
  }
}
