package uk.ac.rhul.cs3821.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BookController.
 */
@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookRepository bookRepository;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AppUserDetailsService uds;

  @Test
  void testGetAllBooks() throws Exception {
    List<Book> mockBooks = List.of(
        new Book(1L, "id1", "Title A", "Author X", "Desc", "cover", "Genre", "goodreads")
    );

    when(bookRepository.findAll()).thenReturn(mockBooks);

    mockMvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Title A"))
        .andExpect(jsonPath("$[0].author").value("Author X"));
  }

  @Test
  void testGetBooksByGenre() throws Exception {
    List<Book> mockBooks = List.of(
        new Book(1L, "id1", "The Silent Patient", "Alex Michaelides", "Desc", "cover1", "Psychological Thrillers", "goodreads"),
        new Book(2L, "id2", "Behind Closed Doors", "B.A. Paris", "Desc", "cover2", "Psychological Thrillers", "goodreads")
    );

    when(bookRepository.findByGenreIgnoreCase(anyString())).thenReturn(mockBooks);

    mockMvc.perform(get("/api/books/genre/Psychological%20Thrillers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].genre").value("Psychological Thrillers"))
        .andExpect(jsonPath("$[1].title").value("Behind Closed Doors"));
  }

  @Test
  void testDeleteBook_Success() throws Exception {
    when(bookRepository.existsById(anyLong())).thenReturn(true);

    mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("Book deleted successfully."));
  }

  @Test
  void testDeleteBook_NotFound() throws Exception {
    when(bookRepository.existsById(anyLong())).thenReturn(false);

    mockMvc.perform(delete("/api/books/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Book not found."));
  }
}