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
 * Service for managing book comments and replies.
 */
@Service
@Transactional
public class CommentService {

  private final BookCommentRepository commentRepo;
  private final NotificationService notificationService;
  private final UserRepository userRepo;
  private final BookRepository bookRepository;
  private final CommentLikeRepository commentLikeRepository;

  /**
   * Constructs a CommentService with required dependencies.
   *
   * @param commentRepo           repository for book comments
   * @param friendshipRepo        repository for friendships
   * @param notificationService   service for sending notifications
   * @param userRepo              repository for users
   * @param bookRepository        repository for books
   * @param commentLikeRepository repository for comment likes
   */
  public CommentService(BookCommentRepository commentRepo, FriendshipRepository friendshipRepo,
                        NotificationService notificationService, UserRepository userRepo,
                        BookRepository bookRepository,
                        CommentLikeRepository commentLikeRepository) {
    this.commentRepo = commentRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
    this.bookRepository = bookRepository;
    this.commentLikeRepository = commentLikeRepository;
  }

  /**
   * Adds a top-level comment or reply to a book.
   * Notifies the parent comment's author when a reply is posted by another user.
   *
   * @param userId          the ID of the commenting user
   * @param bookId          the ID of the book
   * @param content         the comment text
   * @param parentCommentId the ID of the parent comment, or null for top-level comments
   * @return the saved BookComment
   * @throws EntityNotFoundException if the book or parent comment is not found
   */
  public BookComment addComment(Long userId, Long bookId,
                                String content, Long parentCommentId) {
    BookComment comment = new BookComment();
    comment.setUser(userRepo.getReferenceById(userId));

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("Book not found"));
    comment.setBook(book);
    comment.setContent(content);

    if (parentCommentId != null) {
      BookComment parent = commentRepo.findById(parentCommentId)
          .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
      comment.setParentComment(parent);

      if (!parent.getUser().getId().equals(userId)) {
        notificationService.send(parent.getUser().getId(),
            NotificationType.COMMENT_REPLY, parent.getId(), userId);
      }
    }

    return commentRepo.save(comment);
  }

  /**
   * Soft deletes a comment. Only the comment author may delete their own comment.
   *
   * @param commentId     the ID of the comment to delete
   * @param currentUserId the ID of the authenticated user
   * @throws EntityNotFoundException if the comment is not found
   * @throws AccessDeniedException   if the user is not the comment author
   */
  public void deleteComment(Long commentId, Long currentUserId) {
    BookComment comment = commentRepo.findById(commentId)
        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

    if (!comment.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Cannot delete another user's comment");
    }

    comment.setDeleted(true);
    commentRepo.save(comment);
  }

  /**
   * Returns all top-level comments for a book, excluding deleted ones.
   *
   * @param bookId        the ID of the book
   * @param currentUserId the ID of the authenticated user, used to resolve liked status
   * @return list of CommentDto
   */
  @Transactional(readOnly = true)
  public List<CommentDto> getComments(Long bookId, Long currentUserId) {
    return commentRepo.findByBookIdAndParentCommentIsNullAndDeletedFalse(bookId)
        .stream()
        .map(c -> toDto(c, currentUserId))
        .toList();
  }

  /**
   * Maps a BookComment to a CommentDto, including like count and liked status.
   *
   * @param comment       the comment to map
   * @param currentUserId the ID of the authenticated user
   * @return the mapped CommentDto
   */
  @Transactional(readOnly = true)
  public CommentDto toDto(BookComment comment, Long currentUserId) {
    int likeCount = commentLikeRepository.countByCommentId(comment.getId());
    boolean liked = commentLikeRepository
        .existsByUserIdAndCommentId(currentUserId, comment.getId());
    return CommentDto.from(comment, likeCount, liked);
  }
}