package uk.ac.rhul.cs3821.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.rhul.cs3821.dto.GoogleBooksDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {

  @Mock
  private BookRepository repo;

  // WebClient mocks
  @Mock
  private WebClient webClient;
  @Mock
  private WebClient.RequestHeadersUriSpec uriSpec;
  @Mock
  private WebClient.RequestHeadersSpec headersSpec;
  @Mock
  private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private BookService service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    // Replace the real WebClient in BookService with the mock
    try {
      var field = BookService.class.getDeclaredField("web");
      field.setAccessible(true);
      field.set(service, webClient);
    } catch (Exception e) {
      fail("Failed to inject WebClient mock");
    }

    // Default WebClient chain setup for all tests
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
  }

  // ----------------------------------------
  // fetchAndStorePopularFiction (happy path)
  // ----------------------------------------
  @Test
  void testFetchAndStorePopularFiction() {
    GoogleBooksDto dto = new GoogleBooksDto();

    GoogleBooksDto.Item item = new GoogleBooksDto.Item();
    item.id = "id1";

    GoogleBooksDto.VolumeInfo vi = new GoogleBooksDto.VolumeInfo();
    vi.title = "Title A";
    vi.authors = List.of("Author A");
    vi.description = "Desc A";

    GoogleBooksDto.ImageLinks links = new GoogleBooksDto.ImageLinks();
    links.thumbnail = "http://imageA";
    vi.imageLinks = links;

    item.volumeInfo = vi;
    dto.items = List.of(item);

    // Mock repo and WebClient behavior
    when(repo.findByExternalId("id1")).thenReturn(Optional.empty());
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
    when(responseSpec.bodyToMono(GoogleBooksDto.class))
        .thenAnswer(inv -> Mono.just(dto)); // reused for all category calls

    List<Book> result = service.fetchAndStorePopularFiction(10);

    assertFalse(result.isEmpty());
    Book book = result.get(0);
    assertEquals("Title A", book.getTitle());
    assertEquals("Author A", book.getAuthor());
    assertEquals("https://imageA", book.getCoverUrl()); // secure() applied
    assertEquals("google_books", book.getSource());
  }

  // ----------------------------------------
  // dto.items == null
  // ----------------------------------------
  @Test
  void testFetchAndStorePopularFiction_NullItems() {
    GoogleBooksDto dto = new GoogleBooksDto();
    dto.items = null;

    when(responseSpec.bodyToMono(GoogleBooksDto.class)).thenReturn(Mono.just(dto));

    List<Book> result = service.fetchAndStorePopularFiction(5);

    assertTrue(result.isEmpty());
  }

  // ----------------------------------------
  // Upsert behaviour
  // ----------------------------------------
  @Test
  void testUpsert_UpdatesWhenExists() {
    Book existing = Book.builder()
        .externalId("id1")
        .title("Old Title")
        .author("Old Author")
        .build();

    when(repo.findByExternalId("id1")).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    GoogleBooksDto dto = new GoogleBooksDto();
    GoogleBooksDto.Item item = new GoogleBooksDto.Item();
    item.id = "id1";

    GoogleBooksDto.VolumeInfo vi = new GoogleBooksDto.VolumeInfo();
    vi.title = "New Title";
    vi.authors = List.of("New Author");
    item.volumeInfo = vi;

    dto.items = List.of(item);

    when(responseSpec.bodyToMono(GoogleBooksDto.class)).thenAnswer(i -> Mono.just(dto));

    List<Book> result = service.fetchAndStorePopularFiction(3);

    assertEquals("New Title", result.get(0).getTitle());
    assertEquals("New Author", result.get(0).getAuthor());
  }

  // ----------------------------------------
  // getAll()
  // ----------------------------------------
  @Test
  void testGetAll() {
    // Avoid NPE from fetchAndStorePopularFiction(50)
    when(responseSpec.bodyToMono(GoogleBooksDto.class)).thenReturn(Mono.empty());
    when(repo.findAll()).thenReturn(List.of(new Book()));

    List<Book> result = service.getAll();

    assertEquals(1, result.size());
  }

  // ----------------------------------------
// getBooksByGenre()
// ----------------------------------------
  @Test
  void testGetBooksByGenre() {
    List<Book> mockBooks = List.of(
        new Book(1L, "ext1", "The Silent Patient", "Alex Michaelides", "Desc", "url", "Psychological Thrillers", "google_books"),
        new Book(2L, "ext2", "Behind Closed Doors", "B.A. Paris", "Desc", "url", "Psychological Thrillers", "google_books")
    );

    when(repo.findByGenreIgnoreCase("Psychological Thrillers")).thenReturn(mockBooks);

    List<Book> result = service.getBooksByGenre("Psychological Thrillers");

    assertEquals(2, result.size());
    assertEquals("The Silent Patient", result.get(0).getTitle());
    assertEquals("Psychological Thrillers", result.get(0).getGenre());
    verify(repo).findByGenreIgnoreCase("Psychological Thrillers");
  }
  
  // ----------------------------------------
  // deleteBookById
  // ----------------------------------------
  @Test
  void testDeleteBookById_Success() {
    when(repo.existsById(1L)).thenReturn(true);

    boolean result = service.deleteBookById(1L);

    verify(repo).deleteById(1L);
    assertTrue(result);
  }

  @Test
  void testDeleteBookById_NotFound() {
    when(repo.existsById(1L)).thenReturn(false);

    boolean result = service.deleteBookById(1L);

    verify(repo, never()).deleteById(any());
    assertFalse(result);
  }

  // ----------------------------------------
  // secure() static helper
  // ----------------------------------------
  @Test
  void testSecure() throws Exception {
    var method = BookService.class.getDeclaredMethod("secure", String.class);
    method.setAccessible(true);

    assertEquals("https://site", method.invoke(null, "http://site"));
    assertEquals("https://already", method.invoke(null, "https://already"));
    assertNull(method.invoke(null, (String) null));
  }
}
