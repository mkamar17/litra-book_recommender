package uk.ac.rhul.cs3821.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.BookComment;

/**
 * Repository for BookComment entities.
 * Provides queries for retrieving top-level comments and threaded replies.
 */
public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

  /**
   * Returns all non-deleted top-level comments for a book.
   * Top-level comments are those with no parent comment.
   *
   * @param bookId the ID of the book
   * @return list of top-level BookComment entities
   */
  List<BookComment> findByBookIdAndParentCommentIsNullAndDeletedFalse(
      Long bookId);

  /**
   * Returns all non-deleted replies to a specific comment.
   *
   * @param parentCommentId the ID of the parent comment
   * @return list of reply BookComment entities
   */
  List<BookComment> findByParentCommentIdAndDeletedFalse(Long parentCommentId);
}