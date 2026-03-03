package uk.ac.rhul.cs3821.dto;

import java.time.LocalDateTime;
import java.util.List;

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
}