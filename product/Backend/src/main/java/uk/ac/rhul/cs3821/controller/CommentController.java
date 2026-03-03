package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.CommentDto;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.CommentService;

@RestController
@RequestMapping("/api/books/{bookId}/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final UserRepository userRepository;

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  // Paginated top-level comments for a book
  @GetMapping
  public ResponseEntity<Page<CommentDto>> getComments(
      @PathVariable Long bookId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        commentService.getComments(bookId, PageRequest.of(page, size)).map(commentService::toDto));
  }

  // Post a comment — parentCommentId is optional, only needed for replies
  @PostMapping
  public ResponseEntity<CommentDto> postComment(
      @PathVariable Long bookId,
      @RequestBody CommentRequest request) {
    Long userId = getCurrentUser().getId();
    BookComment saved = commentService.addComment(userId, bookId,
        request.content(), request.parentCommentId());
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.toDto(saved));
  }

  // Soft delete — only the comment author can do this
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) throws Exception {
    Long userId = getCurrentUser().getId();
    commentService.deleteComment(commentId, userId);
    return ResponseEntity.noContent().build();
  }

  // Simple record for the request body — no need for a separate DTO file
  public record CommentRequest(String content, Long parentCommentId) {
  }
}