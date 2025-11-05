package uk.ac.rhul.cs3821.model;

import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

  @Test
  void testGettersAndSetters() {
    User user = new User();

    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setRoles(Set.of("USER", "ADMIN"));

    assertEquals(1L, user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("password123", user.getPassword());
    assertTrue(user.getRoles().contains("USER"));
    assertTrue(user.getRoles().contains("ADMIN"));
  }

  @Test
  void testRolesCanBeUpdated() {
    User user = new User();

    user.setRoles(Set.of("USER"));
    assertEquals(1, user.getRoles().size());

    user.setRoles(Set.of("USER", "EDITOR"));
    assertEquals(2, user.getRoles().size());
  }

  @Test
  void testDefaultValuesWhenCreated() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getPassword());
    assertNull(user.getRoles());
  }

  @Test
  void testSetterOverridesPreviousValues() {
    User user = new User();

    user.setEmail("old@example.com");
    user.setEmail("new@example.com");

    assertEquals("new@example.com", user.getEmail());
  }
}
