package uk.ac.rhul.cs3821.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.BookComment;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

  // Top level comments only (no replies) for a book, excluding soft deleted
  Page<BookComment> findByBookIdAndParentCommentIsNullAndDeletedFalse(
      Long bookId, Pageable pageable);

  // All replies to a specific comment
  List<BookComment> findByParentCommentIdAndDeletedFalse(Long parentCommentId);
}