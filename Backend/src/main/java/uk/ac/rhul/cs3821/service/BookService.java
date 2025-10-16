package uk.ac.rhul.cs3821.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.rhul.cs3821.dto.GoogleBooksDTO;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.repository.BookRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repo;
    private final WebClient web = WebClient.builder().baseUrl("https://www.googleapis.com").build();

    public List<Book> fetchAndStorePopularFiction(int max) {
        // You can tweak query to target popular fiction keywords/categories
        String path = "/books/v1/volumes?q=subject:fiction&maxResults=" + Math.min(max, 40);

        uk.ac.rhul.cs3821.dto.GoogleBooksDTO dto = web.get()
                .uri(path)
                .retrieve()
                .bodyToMono(uk.ac.rhul.cs3821.dto.GoogleBooksDTO.class)
                .block();

        if (dto == null || dto.items == null) return List.of();

        List<Book> fetchedBooks = dto.items.stream()
                .map(item -> {
                    String title = item.volumeInfo != null ? item.volumeInfo.title : null;
                    String author = (item.volumeInfo != null && item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                            ? item.volumeInfo.authors.get(0) : "Unknown";
                    String desc = item.volumeInfo != null ? item.volumeInfo.description : null;
                    String genre = (item.volumeInfo != null && item.volumeInfo.categories != null && !item.volumeInfo.categories.isEmpty())
                            ? item.volumeInfo.categories.get(0) : "Fiction";
                    String cover = (item.volumeInfo != null && item.volumeInfo.imageLinks != null)
                            ? secure(item.volumeInfo.imageLinks.thumbnail) : null;

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

    private Book upsertByExternalId(Book candidate) {
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

    private static String secure(String url) {
        if (url == null) return null;
        return url.startsWith("http://") ? url.replace("http://", "https://") : url;
    }

    public List<Book> getAll() { return repo.findAll(); }
}
