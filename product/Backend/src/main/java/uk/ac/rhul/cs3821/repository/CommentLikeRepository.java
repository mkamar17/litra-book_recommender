package uk.ac.rhul.cs3821.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.CommentLike;

/**
 * Repository for CommentLike entities.
 * Provides queries for checking, removing, and counting likes on comments.
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  /**
   * Returns whether a user has already liked a specific comment.
   *
   * @param userId    the ID of the user
   * @param commentId the ID of the comment
   * @return true if the like exists, false otherwise
   */
  boolean existsByUserIdAndCommentId(Long userId, Long commentId);

  /**
   * Removes a like from a comment by a specific user.
   *
   * @param userId    the ID of the user
   * @param commentId the ID of the comment
   */
  void deleteByUserIdAndCommentId(Long userId, Long commentId);

  /**
   * Returns the total number of likes on a specific comment.
   *
   * @param commentId the ID of the comment
   * @return the like count
   */
  int countByCommentId(Long commentId);
}