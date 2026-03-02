package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.BookCommentRepository;
import uk.ac.rhul.cs3821.repository.FriendshipRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

@Service
@Transactional
public class CommentService {

  private final BookCommentRepository commentRepo;
  //private final FriendshipRepository friendshipRepo;
  private final NotificationService notificationService;
  private final UserRepository userRepo;

  public CommentService(BookCommentRepository commentRepo, FriendshipRepository friendshipRepo, NotificationService notificationService, UserRepository userRepo) {
    this.commentRepo = commentRepo;
    //this.friendshipRepo = friendshipRepo;
    this.notificationService = notificationService;
    this.userRepo = userRepo;
  }

  public BookComment addComment(Long userId, String bookId,
                                String content, Long parentCommentId) {
    BookComment comment = new BookComment();
    comment.setUser(userRepo.getReferenceById(userId));
    comment.setBookId(bookId);
    comment.setContent(content);

    if (parentCommentId != null) {
      BookComment parent = commentRepo.findById(parentCommentId)
          .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
      comment.setParentComment(parent);

      // Notify the original commenter of the reply
      if (!parent.getUser().getId().equals(userId))
        notificationService.send(parent.getUser().getId(),
            NotificationType.COMMENT_REPLY, parent.getId());
    }

    return commentRepo.save(comment);
  }

  public void deleteComment(Long commentId, Long currentUserId) throws AccessDeniedException {
    BookComment comment = commentRepo.findById(commentId)
        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

    if (!comment.getUser().getId().equals(currentUserId))
      throw new AccessDeniedException("Cannot delete another user's comment");

    comment.setDeleted(true);  // soft delete — preserves reply threads
    commentRepo.save(comment);
  }
}
