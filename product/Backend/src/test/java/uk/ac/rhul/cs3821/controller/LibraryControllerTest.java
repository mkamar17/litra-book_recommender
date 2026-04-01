package uk.ac.rhul.cs3821.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LibraryControllerTest {

  @Mock
  private UserRepository userRepo;

  @Mock
  private BookRepository bookRepo;

  @Mock
  private Principal principal;

  @InjectMocks
  private LibraryController libraryController;

  private User testUser;
  private Book testBook;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setLibrary(new HashSet<>());

    testBook = new Book();
    testBook.setId(1L);
    testBook.setTitle("Test Book");
  }

  @Test
  void testAddBookToLibrarySuccess() {
    when(principal.getName()).thenReturn("test@example.com");
    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(bookRepo.findById(1L)).thenReturn(Optional.of(testBook));

    ResponseEntity<?> response = libraryController.addBookToLibrary(1L, principal);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Book added to library", response.getBody());
    assertTrue(testUser.getLibrary().contains(testBook));
    verify(userRepo, times(1)).save(testUser);
  }

  @Test
  void testAddBookToLibraryUserNotFound() {
    when(principal.getName()).thenReturn("missing@example.com");
    when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        libraryController.addBookToLibrary(1L, principal)
    );

    assertEquals("User not found", exception.getMessage());
    verify(userRepo, never()).save(any());
  }

  @Test
  void testAddBookToLibraryBookNotFound() {
    when(principal.getName()).thenReturn("test@example.com");
    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(bookRepo.findById(99L)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        libraryController.addBookToLibrary(99L, principal)
    );

    assertEquals("Book not found", exception.getMessage());
    verify(userRepo, never()).save(any());
  }

  @Test
  void testGetUserLibrarySuccess() {
    when(principal.getName()).thenReturn("test@example.com");
    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    testUser.getLibrary().add(testBook);

    ResponseEntity<?> response = libraryController.getUserLibrary(principal);

    assertEquals(200, response.getStatusCodeValue());
    assertTrue(response.getBody() instanceof HashSet);
    assertTrue(((HashSet<?>) response.getBody()).contains(testBook));
  }

  @Test
  void testGetUserLibraryUserNotFound() {
    when(principal.getName()).thenReturn("missing@example.com");
    when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        libraryController.getUserLibrary(principal)
    );

    assertEquals("User not found", exception.getMessage());
  }
}