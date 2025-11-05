package uk.ac.rhul.cs3821.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.rhul.cs3821.dto.GoogleBooksDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;

/**
 * Service layer for interacting with Google Books and managing {@link Book} entities.
 */
@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository repo;
  private final WebClient web = WebClient.builder()
      .baseUrl("https://www.googleapis.com")
      .build();

  private static String secure(final String url) {
    if (url == null) {
      return null;
    }
    return url.startsWith("http://")
        ? url.replace("http://", "https://")
        : url;
  }

  /**
   * Fetches popular fiction books from Google Books, stores them locally,
   * and returns a unified list.
   *
   * @param max maximum number of results to fetch
   * @return list of stored {@link Book} entities
   */
  public List<Book> fetchAndStorePopularFiction(final int max) {
    final String path = "/books/v1/volumes?q=subject:fiction&maxResults="
        + Math.min(max, 40);

    final GoogleBooksDto dto = web.get()
        .uri(path)
        .retrieve()
        .bodyToMono(GoogleBooksDto.class)
        .block();

    if (dto == null || dto.items == null) {
      return List.of();
    }

    final List<Book> fetchedBooks = dto.items.stream()
        .map(item -> {
          final String title = item.volumeInfo != null
              ? item.volumeInfo.title : null;
          final String author =
              (item.volumeInfo != null
                  && item.volumeInfo.authors != null
                  && !item.volumeInfo.authors.isEmpty())
                  ? item.volumeInfo.authors.get(0) : "Unknown";
          final String desc = item.volumeInfo != null
              ? item.volumeInfo.description : null;
          final String genre =
              (item.volumeInfo != null
                  && item.volumeInfo.categories != null
                  && !item.volumeInfo.categories.isEmpty())
                  ? item.volumeInfo.categories.getFirst() : "Fiction";
          final String cover =
              (item.volumeInfo != null
                  && item.volumeInfo.imageLinks != null)
                  ? secure(item.volumeInfo.imageLinks.thumbnail)
                  : null;

          return Book.builder()
              .externalId(item.id)
              .title(title)
              .author(author)
              .description(desc)
              .genre(genre)
              .coverUrl(cover)
              .source("google_books")
              .build();
        })
        .toList();

    return fetchedBooks.stream()
        .map(this::upsertByExternalId)
        .collect(Collectors.toList());
  }

  private Book upsertByExternalId(final Book candidate) {
    return repo.findByExternalId(candidate.getExternalId())
        .map(existing -> {
          existing.setTitle(candidate.getTitle());
          existing.setAuthor(candidate.getAuthor());
          existing.setDescription(candidate.getDescription());
          existing.setGenre(candidate.getGenre());
          existing.setCoverUrl(candidate.getCoverUrl());
          existing.setSource(candidate.getSource());
          return repo.save(existing);
        })
        .orElseGet(() -> repo.save(candidate));
  }

  /**
   * Retrieves all stored books.
   *
   * @return list of books
   */
  public List<Book> getAll() {
    return repo.findAll();
  }

  /**
   * Deletes a book by ID if it exists.
   *
   * @param id identifier of the book to delete
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteBookById(final Long id) {
    if (repo.existsById(id)) {
      repo.deleteById(id);
      return true;
    }
    return false;
  }
}
