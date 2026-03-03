package uk.ac.rhul.cs3821.dto;

import java.time.LocalDateTime;
import java.util.List;
import uk.ac.rhul.cs3821.model.BookComment;

public class CommentDto {
  public Long id;
  public String authorEmail;
  public String content;
  public LocalDateTime createdAt;
  public List<CommentDto> replies;
  public int likeCount;

  public CommentDto(Long id, String authorEmail, String content,
                    LocalDateTime createdAt, List<CommentDto> replies, int likeCount) {
    this.id = id;
    this.authorEmail = authorEmail;
    this.content = content;
    this.createdAt = createdAt;
    this.replies = replies;
    this.likeCount = likeCount;
  }

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