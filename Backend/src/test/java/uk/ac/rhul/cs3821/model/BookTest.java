package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    void testBuilderCreatesCorrectBook() {
        Book book = Book.builder()
                .id(1L)
                .externalId("abc123")
                .title("Dystopian Futures")
                .author("George Orwell")
                .description("A novel about a dystopian society.")
                .coverUrl("http://example.com/cover.jpg")
                .genre("Dystopian")
                .source("google_books")
                .build();

        assertEquals(1L, book.getId());
        assertEquals("abc123", book.getExternalId());
        assertEquals("Dystopian Futures", book.getTitle());
        assertEquals("George Orwell", book.getAuthor());
        assertEquals("A novel about a dystopian society.", book.getDescription());
        assertEquals("http://example.com/cover.jpg", book.getCoverUrl());
        assertEquals("Dystopian", book.getGenre());
        assertEquals("google_books", book.getSource());
    }

    @Test
    void testSettersAndGetters() {
        Book book = new Book();
        book.setId(10L);
        book.setExternalId("vol123");
        book.setTitle("Neuromancer");
        book.setAuthor("William Gibson");
        book.setDescription("Cyberpunk classic.");
        book.setCoverUrl("http://example.com/neuromancer.jpg");
        book.setGenre("Cyberpunk");
        book.setSource("google_books");

        assertEquals(10L, book.getId());
        assertEquals("vol123", book.getExternalId());
        assertEquals("Neuromancer", book.getTitle());
        assertEquals("William Gibson", book.getAuthor());
        assertEquals("Cyberpunk classic.", book.getDescription());
        assertEquals("http://example.com/neuromancer.jpg", book.getCoverUrl());
        assertEquals("Cyberpunk", book.getGenre());
        assertEquals("google_books", book.getSource());
    }

    @Test
    void testNoArgsConstructor() {
        Book book = new Book();
        assertNull(book.getId());
        assertNull(book.getTitle());
    }

    @Test
    void testAllArgsConstructor() {
        Book book = new Book(5L, "ext789", "Brave New World", "Aldous Huxley",
                "Another dystopian novel.", "http://example.com/bravenewworld.jpg", "Dystopian", "google_books");

        assertEquals(5L, book.getId());
        assertEquals("ext789", book.getExternalId());
        assertEquals("Brave New World", book.getTitle());
        assertEquals("Aldous Huxley", book.getAuthor());
    }
}
