package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommentLikeTest {

  private User user;
  private BookComment comment;
  private CommentLike like;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("liker@example.com");

    Book book = new Book();
    book.setId(1L);

    comment = new BookComment();
    comment.setUser(user);
    comment.setBook(book);
    comment.setContent("Great book!");

    like = new CommentLike();
    like.setUser(user);
    like.setComment(comment);
  }

  @Test
  void settersAndGettersWork() {
    assertEquals(user, like.getUser());
    assertEquals(comment, like.getComment());
  }

  @Test
  void likeCanBeReassignedToAnotherUser() {
    User otherUser = new User();
    otherUser.setId(2L);
    otherUser.setEmail("other@example.com");

    like.setUser(otherUser);
    assertEquals(otherUser, like.getUser());
  }

  @Test
  void likeCanBeReassignedToAnotherComment() {
    BookComment otherComment = new BookComment();
    otherComment.setContent("Another comment");

    like.setComment(otherComment);
    assertEquals(otherComment, like.getComment());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    CommentLike empty = new CommentLike();
    assertNull(empty.getId());
    assertNull(empty.getUser());
    assertNull(empty.getComment());
  }
}