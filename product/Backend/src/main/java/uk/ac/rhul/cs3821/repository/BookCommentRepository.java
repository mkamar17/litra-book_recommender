package uk.ac.rhul.cs3821.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.BookComment;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

  // book comments with no replies
  List<BookComment> findByBookIdAndParentCommentIsNullAndDeletedFalse(
      Long bookId);

  // replies to a comment
  List<BookComment> findByParentCommentIdAndDeletedFalse(Long parentCommentId);
}