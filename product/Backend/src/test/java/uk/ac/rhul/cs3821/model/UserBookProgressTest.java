package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserBookProgressTest {

  @Test
  void builderCreatesUserBookProgressCorrectly() {
    User user = new User();
    Book book = new Book();

    UserBookProgress progress = UserBookProgress.builder()
        .user(user)
        .book(book)
        .totalPages(300)
        .currentPage(50)
        .build();

    assertEquals(user, progress.getUser());
    assertEquals(book, progress.getBook());
    assertEquals(300, progress.getTotalPages());
    assertEquals(50, progress.getCurrentPage());
  }

  @Test
  void settersUpdateFieldsCorrectly() {
    UserBookProgress progress = new UserBookProgress();

    User user = new User();
    Book book = new Book();

    progress.setUser(user);
    progress.setBook(book);
    progress.setTotalPages(200);
    progress.setCurrentPage(20);

    assertEquals(user, progress.getUser());
    assertEquals(book, progress.getBook());
    assertEquals(200, progress.getTotalPages());
    assertEquals(20, progress.getCurrentPage());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    UserBookProgress progress = new UserBookProgress();

    assertNull(progress.getId());
    assertNull(progress.getUser());
    assertNull(progress.getBook());
    assertEquals(0, progress.getTotalPages());
    assertEquals(0, progress.getCurrentPage());
  }

  @Test
  void allArgsConstructorSetsAllFields() {
    User user = new User();
    Book book = new Book();

    UserBookProgress progress = new UserBookProgress(
        1L,
        user,
        book,
        150,
        30
    );

    assertEquals(1L, progress.getId());
    assertEquals(user, progress.getUser());
    assertEquals(book, progress.getBook());
    assertEquals(150, progress.getTotalPages());
    assertEquals(30, progress.getCurrentPage());
  }
}
