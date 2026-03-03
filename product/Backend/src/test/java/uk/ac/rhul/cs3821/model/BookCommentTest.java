package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookCommentTest {

  private User user;
  private Book book;
  private BookComment comment;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("reader@example.com");

    book = new Book();
    book.setId(1L);
    book.setTitle("1984");

    comment = new BookComment();
    comment.setUser(user);
    comment.setBook(book);
    comment.setContent("A chilling read.");
  }

  @Test
  void settersAndGettersWork() {
    assertEquals(user, comment.getUser());
    assertEquals(book, comment.getBook());
    assertEquals("A chilling read.", comment.getContent());
  }

  @Test
  void notDeletedByDefault() {
    assertFalse(comment.isDeleted());
  }

  @Test
  void canBeSoftDeleted() {
    comment.setDeleted(true);
    assertTrue(comment.isDeleted());
  }

  @Test
  void parentCommentIsNullForTopLevel() {
    assertNull(comment.getParentComment());
  }

  @Test
  void replyLinksToParent() {
    BookComment reply = new BookComment();
    reply.setUser(user);
    reply.setBook(book);
    reply.setContent("Totally agree!");
    reply.setParentComment(comment);

    assertEquals(comment, reply.getParentComment());
  }

  @Test
  void repliesListIsEmptyByDefault() {
    assertTrue(comment.getReplies().isEmpty());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    BookComment empty = new BookComment();
    assertNull(empty.getId());
    assertNull(empty.getUser());
    assertNull(empty.getContent());
    assertFalse(empty.isDeleted());
  }
}