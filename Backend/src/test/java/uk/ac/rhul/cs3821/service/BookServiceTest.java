package uk.ac.rhul.cs3821.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.rhul.cs3821.dto.GoogleBooksDTO;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

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

        // Replace real WebClient with mock via reflection
        try {
            var field = BookService.class.getDeclaredField("web");
            field.setAccessible(true);
            field.set(service, webClient);
        } catch (Exception e) {
            fail("Failed to inject WebClient mock");
        }
    }

    // ----------------------------------------
    // fetchAndStorePopularFiction (happy path)
    // ----------------------------------------
    @Test
    void testFetchAndStorePopularFiction() {
        GoogleBooksDTO dto = new GoogleBooksDTO();

        GoogleBooksDTO.Item item = new GoogleBooksDTO.Item();
        item.id = "id1";

        GoogleBooksDTO.VolumeInfo vi = new GoogleBooksDTO.VolumeInfo();
        vi.title = "Title A";
        vi.authors = List.of("Author A");
        vi.description = "Desc A";
        vi.categories = List.of("Fiction");

        GoogleBooksDTO.ImageLinks links = new GoogleBooksDTO.ImageLinks();
        links.thumbnail = "http://imageA";
        vi.imageLinks = links;

        item.volumeInfo = vi;
        dto.items = List.of(item);

        // repo upsert behaviour
        when(repo.findByExternalId("id1")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        // WebClient chain mocks
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleBooksDTO.class)).thenReturn(Mono.just(dto));

        List<Book> result = service.fetchAndStorePopularFiction(10);

        assertEquals(1, result.size());
        assertEquals("Title A", result.get(0).getTitle());
        assertEquals("Author A", result.get(0).getAuthor());
        assertEquals("https://imageA", result.get(0).getCoverUrl()); // secure() applied
        assertEquals("google_books", result.get(0).getSource());
    }

    // ----------------------------------------
    // dto.items == null
    // ----------------------------------------
    @Test
    void testFetchAndStorePopularFiction_NullItems() {
        GoogleBooksDTO dto = new GoogleBooksDTO();
        dto.items = null;

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleBooksDTO.class)).thenReturn(Mono.just(dto));

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

        // DTO with new info
        GoogleBooksDTO dto = new GoogleBooksDTO();
        GoogleBooksDTO.Item item = new GoogleBooksDTO.Item();
        item.id = "id1";

        GoogleBooksDTO.VolumeInfo vi = new GoogleBooksDTO.VolumeInfo();
        vi.title = "New Title";
        vi.authors = List.of("New Author");

        item.volumeInfo = vi;
        dto.items = List.of(item);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleBooksDTO.class)).thenReturn(Mono.just(dto));

        List<Book> result = service.fetchAndStorePopularFiction(3);

        assertEquals("New Title", result.get(0).getTitle());
        assertEquals("New Author", result.get(0).getAuthor());
    }

    // ----------------------------------------
    // getAll()
    // ----------------------------------------
    @Test
    void testGetAll() {
        when(repo.findAll()).thenReturn(List.of(new Book()));
        assertEquals(1, service.getAll().size());
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
