package uk.ac.rhul.cs3821.dto;

import java.time.LocalDateTime;
import java.util.List;
import uk.ac.rhul.cs3821.model.BookComment;

/**
 * Dto for returning comment data in API responses.
 */

public class CommentDto {
  public Long id;
  public String authorEmail;
  public String content;
  public LocalDateTime createdAt;
  public List<CommentDto> replies;
  public int likeCount;

  /**
   * Constructs a CommentDto with all fields.
   *
   * @param id          the comment ID
   * @param authorEmail the author's email
   * @param content     the comment text
   * @param createdAt   the creation timestamp
   * @param replies     nested reply DTOs
   * @param likeCount   number of likes
   */
  public CommentDto(Long id, String authorEmail, String content,
                    LocalDateTime createdAt, List<CommentDto> replies, int likeCount) {
    this.id = id;
    this.authorEmail = authorEmail;
    this.content = content;
    this.createdAt = createdAt;
    this.replies = replies;
    this.likeCount = likeCount;
  }

  /**
   * Converts a BookComment entity to a CommentDto.
   * Deleted comments display "[deleted]" as their content.
   * Replies are mapped recursively with a like count of zero.
   *
   * @param comment   the comment entity to convert
   * @param likeCount the number of likes for this comment
   * @return the mapped CommentDto
   */
  public static CommentDto from(BookComment comment, int likeCount) {
    List<CommentDto> replyDtos = comment.getReplies().stream()
        .filter(r -> !r.isDeleted())
        .map(r -> CommentDto.from(r, 0))  // replies don't need like count for now
        .toList();

    return new CommentDto(
        comment.getId(),
        comment.getUser().getEmail(),
        comment.isDeleted() ? "[deleted]" : comment.getContent(),
        comment.getCreatedAt(),
        replyDtos,
        likeCount
    );
  }
}