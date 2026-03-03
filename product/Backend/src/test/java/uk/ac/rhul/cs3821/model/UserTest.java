package uk.ac.rhul.cs3821.model;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setRoles(Set.of("USER"));
  }

  @Test
  void gettersReturnCorrectValues() {
    assertEquals(1L, user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("password123", user.getPassword());
    assertTrue(user.getRoles().contains("USER"));
  }

  @Test
  void rolesCanBeUpdated() {
    user.setRoles(Set.of("USER", "EDITOR"));
    assertEquals(2, user.getRoles().size());
    assertTrue(user.getRoles().contains("EDITOR"));
  }

  @Test
  void defaultPointsAreZero() {
    User fresh = new User();
    assertEquals(0, fresh.getTotalPoints());
    assertEquals(0, fresh.getWeeklyPoints());
    assertEquals(0, fresh.getMonthlyPoints());
  }

  @Test
  void pointsCanBeUpdated() {
    user.setTotalPoints(500);
    user.setWeeklyPoints(100);
    user.setMonthlyPoints(300);

    assertEquals(500, user.getTotalPoints());
    assertEquals(100, user.getWeeklyPoints());
    assertEquals(300, user.getMonthlyPoints());
  }

  @Test
  void emailCanBeOverwritten() {
    user.setEmail("new@example.com");
    assertEquals("new@example.com", user.getEmail());
  }

  @Test
  void defaultsAreNullWhenCreated() {
    User fresh = new User();
    assertNull(fresh.getId());
    assertNull(fresh.getEmail());
    assertNull(fresh.getPassword());
    assertNull(fresh.getRoles());
  }

  @Test
  void libraryCanBeAssignedAndModified() {
    Book book1 = new Book();
    book1.setId(1L);
    Book book2 = new Book();
    book2.setId(2L);

    Set<Book> bookSet = new HashSet<>();
    bookSet.add(book1);
    user.setLibrary(bookSet);

    assertEquals(1, user.getLibrary().size());
    user.getLibrary().add(book2);
    assertEquals(2, user.getLibrary().size());
    assertTrue(user.getLibrary().contains(book2));
  }

  @Test
  void libraryCanBeSetToNull() {
    user.setLibrary(new HashSet<>());
    assertNotNull(user.getLibrary());
    user.setLibrary(null);
    assertNull(user.getLibrary());
  }
}