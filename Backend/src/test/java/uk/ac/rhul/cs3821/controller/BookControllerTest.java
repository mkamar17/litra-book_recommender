package uk.ac.rhul.cs3821.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.BookService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false) //to stop unauthorisation errors due to default security configs
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService service = Mockito.mock(BookService.class);

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AppUserDetailsService uds;

  @Test
  void testFetchBooks() throws Exception {
    List<Book> mockBooks = List.of(
        new Book(1L, "vol123", "Book One", "Author A", "Desc A", "urlA", "Fiction", "google_books"),
        new Book(2L, "vol456", "Book Two", "Author B", "Desc B", "urlB", "Thriller", "google_books")
    );

    when(service.fetchAndStorePopularFiction(anyInt())).thenReturn(mockBooks);

    mockMvc.perform(post("/api/books/fetch")
            .param("max", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("Book One"))
        .andExpect(jsonPath("$[1].title").value("Book Two"));
  }

  @Test
  void testGetAllBooks() throws Exception {
    List<Book> mockBooks = List.of(
        new Book(1L, "id1", "Title A", "Author X", "Desc", "cover", "Genre", "google_books")
    );

    when(service.getAll()).thenReturn(mockBooks);

    mockMvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Title A"))
        .andExpect(jsonPath("$[0].author").value("Author X"));
  }

  @Test
  void testDeleteBook_Success() throws Exception {
    when(service.deleteBookById(anyLong())).thenReturn(true);

    mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("Book deleted successfully."));
  }

  @Test
  void testDeleteBook_NotFound() throws Exception {
    when(service.deleteBookById(anyLong())).thenReturn(false);

    mockMvc.perform(delete("/api/books/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Book not found."));
  }
}
