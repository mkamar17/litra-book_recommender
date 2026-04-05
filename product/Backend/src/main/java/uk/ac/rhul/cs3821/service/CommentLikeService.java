package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.CommentLike;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookCommentRepository;
import uk.ac.rhul.cs3821.repository.CommentLikeRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

@Service
@Transactional
public class CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final BookCommentRepository commentRepo;
  private final UserRepository userRepo;

  public CommentLikeService(CommentLikeRepository commentLikeRepository,
                            BookCommentRepository commentRepo,
                            UserRepository userRepo) {
    this.commentLikeRepository = commentLikeRepository;
    this.commentRepo = commentRepo;
    this.userRepo = userRepo;
  }

  public int toggleLike(Long userId, Long commentId) {
    if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
      commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
    } else {
      User user = userRepo.getReferenceById(userId);
      BookComment comment = commentRepo.findById(commentId)
          .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

      CommentLike like = new CommentLike();
      like.setUser(user);
      like.setComment(comment);
      commentLikeRepository.save(like);
    }

    return commentLikeRepository.countByCommentId(commentId);
  }
}