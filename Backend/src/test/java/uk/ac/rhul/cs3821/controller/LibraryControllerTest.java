//package uk.ac.rhul.cs3821.controller;
//
//import java.util.HashSet;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//import uk.ac.rhul.cs3821.model.Book;
//import uk.ac.rhul.cs3821.model.User;
//import uk.ac.rhul.cs3821.repository.BookRepository;
//import uk.ac.rhul.cs3821.repository.UserRepository;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class LibraryControllerTest {
//  @Mock
//  private UserRepository userRepo;
//
//  @Mock
//  private BookRepository bookRepo;
//
//  @InjectMocks
//  private LibraryController libraryController;
//
//  private User testUser;
//  private Book testBook;
//
//  @BeforeEach
//  void setup() {
//    MockitoAnnotations.openMocks(this);
//
//    testUser = new User();
//    testUser.setEmail("test@example.com");
//    testUser.setLibrary(new HashSet<>());
//
//    testBook = new Book();
//    testBook.setId(1L);
//    testBook.setTitle("Test Book");
//
//    testUser.getLibrary().add(testBook);
//  }
//
//  @Test
//  void testAddBookToLibrarySuccess() {
//    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
//    when(bookRepo.findById(1L)).thenReturn(Optional.of(testBook));
//
//    ResponseEntity<?> response = libraryController.addBookToLibrary(1L, "test@example.com");
//
//    assertEquals(200, response.getStatusCodeValue());
//    assertEquals("Book added to library", response.getBody());
//    assertTrue(testUser.getLibrary().contains(testBook));
//
//    verify(userRepo, times(1)).save(testUser);
//  }
//
//  @Test
//  void testAddBookToLibraryUserNotFound() {
//    when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
//
//    RuntimeException exception = assertThrows(RuntimeException.class, () ->
//        libraryController.addBookToLibrary(1L, "missing@example.com")
//    );
//
//    assertEquals("User not found", exception.getMessage());
//    verify(userRepo, never()).save(any());
//  }
//
//  @Test
//  void testAddBookToLibraryBookNotFound() {
//    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
//    when(bookRepo.findById(99L)).thenReturn(Optional.empty());
//
//    RuntimeException exception = assertThrows(RuntimeException.class, () ->
//        libraryController.addBookToLibrary(99L, "test@example.com")
//    );
//
//    assertEquals("Book not found", exception.getMessage());
//    verify(userRepo, never()).save(any());
//  }
//
//  @Test
//  void testGetUserLibrarySuccess() {
//    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
//
//    ResponseEntity<?> response = libraryController.getUserLibrary("test@example.com");
//
//    assertEquals(200, response.getStatusCodeValue());
//
//    assertInstanceOf(HashSet.class, response.getBody());
//    HashSet<?> books = (HashSet<?>) response.getBody();
//
//    assertEquals(1, books.size());
//    assertTrue(books.contains(testBook));
//  }
//
//  @Test
//  void testGetUserLibraryUserNotFound() {
//    when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
//
//    RuntimeException exception = assertThrows(RuntimeException.class, () ->
//        libraryController.getUserLibrary("missing@example.com")
//    );
//
//    assertEquals("User not found", exception.getMessage());
//  }
//}
