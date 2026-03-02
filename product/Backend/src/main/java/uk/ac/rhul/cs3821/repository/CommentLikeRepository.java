package uk.ac.rhul.cs3821.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  boolean existsByUserIdAndCommentId(Long userId, Long commentId);

  void deleteByUserIdAndCommentId(Long userId, Long commentId);

  int countByCommentId(Long commentId);
}