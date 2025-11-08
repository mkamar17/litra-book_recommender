package uk.ac.rhul.cs3821.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.rhul.cs3821.dto.GoogleBooksDto;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;

/**
 * Service layer for interacting with Google Books and managing Book entities.
 */
@Service
@RequiredArgsConstructor
public class BookService {

  //temporary helper method to get more specific book titles
  private static final Map<String, String> CATEGORIES = Map.of(
      "BookTok Favourites",
      "(intitle:\"It Ends With Us\" OR intitle:\"Verity\" OR intitle:\"Ugly Love\" OR intitle:\"Reminders of Him\" OR inauthor:\"Colleen Hoover\" OR inauthor:\"Ali Hazelwood\" OR \"booktok\")",
      "Psychological Thrillers",
      "(intitle:\"The Silent Patient\" OR intitle:\"Behind Closed Doors\" OR intitle:\"The Housemaid\" OR inauthor:\"B.A. Paris\" OR inauthor:\"Freida McFadden\" OR \"psychological thriller\")",
      "Fantasy & YA",
      "(intitle:\"Shatter Me\" OR intitle:\"A Court of Thorns and Roses\" OR intitle:\"Throne of Glass\" OR intitle:\"The Cruel Prince\" OR inauthor:\"Sarah J. Maas\" OR inauthor:\"Tahereh Mafi\" OR inauthor:\"Holly Black\")",
      "Modern Romance",
      "(intitle:\"Happy Place\" OR intitle:\"Love and Other Words\" OR inauthor:\"Emily Henry\" OR inauthor:\"Taylor Jenkins Reid\" OR \"romance bestseller\")"
  );
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
    List<Book> allBooks = new ArrayList<>();

    for (Map.Entry<String, String> entry : CATEGORIES.entrySet()) {
      String category = entry.getKey();
      String query = entry.getValue();

      System.out.println(">>> Fetching category: " + category);

      final String uri = UriComponentsBuilder.fromPath("/books/v1/volumes")
          .queryParam("q", query)
          .queryParam("filter", "paid-ebooks")
          .queryParam("langRestrict", "en")
          .queryParam("printType", "books")
          .queryParam("orderBy", "relevance")
          .queryParam("maxResults", Math.min(max, 40))
          .build()
          .toUriString();

      final GoogleBooksDto dto = web.get()
          .uri(uri)
          .retrieve()
          .bodyToMono(GoogleBooksDto.class)
          .block();

      if (dto == null || dto.items == null || dto.items.isEmpty()) {
        System.out.println(">>> No results for " + category);
        continue;
      }

      final List<Book> batch = dto.items.stream().map(item -> {
        final String title = item.volumeInfo != null ? item.volumeInfo.title : null;
        final String author =
            (item.volumeInfo != null && item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                ? item.volumeInfo.authors.get(0)
                : "Unknown";
        final String desc = item.volumeInfo != null ? item.volumeInfo.description : null;
        final String safeDesc = (desc != null && desc.length() > 4000)
            ? desc.substring(0, 4000)
            : desc;
        final String cover =
            (item.volumeInfo != null && item.volumeInfo.imageLinks != null)
                ? secure(item.volumeInfo.imageLinks.thumbnail)
                : null;

        return Book.builder()
            .externalId(item.id)
            .title(title)
            .author(author)
            .description(safeDesc)
            .genre(category) // use our curated genre name
            .coverUrl(cover)
            .source("google_books")
            .build();
      }).toList();
      // ✅ Upsert each book to prevent duplicates
      batch.forEach(book -> {
        Book saved = upsertByExternalId(book);
        allBooks.add(saved);
      });

      System.out.println(">>> Added " + batch.size() + " books for " + category);
    }

    // ✅ Remove duplicates across all categories
    List<Book> distinctBooks = allBooks.stream()
        .collect(Collectors.collectingAndThen(
            Collectors.toMap(Book::getExternalId, b -> b, (b1, b2) -> b1),
            m -> new ArrayList<>(m.values())
        ));

    System.out.println(">>> Finished fetching " + distinctBooks.size() + " unique books total.");
    return distinctBooks;
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
    System.out.println("Fetching books...");
    fetchAndStorePopularFiction(50);
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
