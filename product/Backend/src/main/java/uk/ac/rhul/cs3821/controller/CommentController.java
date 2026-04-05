package uk.ac.rhul.cs3821.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.CommentDto;
import uk.ac.rhul.cs3821.model.BookComment;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.CommentLikeService;
import uk.ac.rhul.cs3821.service.CommentService;

/**
 * REST controller for managing book comments.
 * Handles top-level comments and threaded replies on books.
 */
@RestController
@RequestMapping("/api/books/{bookId}/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final CommentLikeService commentLikeService;
  private final UserRepository userRepository;

  /**
   * Gets the current authenticated user from security context.
   *
   * @return the authenticated user.
   */
  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  /**
   * Returns all top-level comments for a book.
   *
   * @param bookId the id of the book
   * @return list of comments
   */
  @GetMapping
  public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long bookId) {
    Long userId = getCurrentUser().getId();
    return ResponseEntity.ok(commentService.getComments(bookId, userId));
  }

  /**
   * Posts a comment on a book. If parentCommentId is provided, the comment is treated as a reply.
   *
   * @param bookId  the id of the book
   * @param request the comment content and optional parent comment id
   * @return the created CommentDto
   */
  @PostMapping
  public ResponseEntity<CommentDto> postComment(
      @PathVariable Long bookId,
      @RequestBody CommentRequest request) {
    Long userId = getCurrentUser().getId();
    BookComment saved = commentService.addComment(userId, bookId,
        request.content(), request.parentCommentId());
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.toDto(saved, userId));
  }

  /**
   * Soft deletes a comment. Only the comment author can delete their own comment.
   *
   * @param commentId the id of the comment to delete
   * @return success HTTP
   * @throws Exception if error on delete
   */
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) throws Exception {
    Long userId = getCurrentUser().getId();
    commentService.deleteComment(commentId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Toggles a like on a comment for the current user.
   *
   * @param commentId the ID of the comment to like or unlike
   * @return the updated like count
   */
  
  @PostMapping("/{commentId}/like")
  public ResponseEntity<Integer> toggleLike(@PathVariable Long commentId) {
    Long userId = getCurrentUser().getId();
    int newCount = commentLikeService.toggleLike(userId, commentId);
    return ResponseEntity.ok(newCount);
  }

  /**
   * Request body for posting a comment or reply.
   *
   * @param content         the comment text
   * @param parentCommentId optional id of the parent comment; null for top-level comments
   */
  public record CommentRequest(String content, Long parentCommentId) {
  }
}