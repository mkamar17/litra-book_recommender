package uk.ac.rhul.cs3821.controller;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.BookRating;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.BookRatingRepository;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for RatingController.
 */
@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookRatingRepository ratingRepo;
  @MockitoBean
  private BookRepository bookRepo;
  @MockitoBean
  private UserRepository userRepo;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private AppUserDetailsService uds;

  private User user;
  private Book book;

  /**
   * Sets up a test user and book, and stubs the user repository lookup.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    book = new Book();
    book.setId(1L);
    book.setTitle("Test Book");

    when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getRating_returnsRatingSummary() throws Exception {
    BookRating rating = BookRating.builder().user(user).book(book).rating((short) 4).build();

    when(ratingRepo.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(rating));
    when(ratingRepo.findAvgRatingByBookId(1L)).thenReturn(Optional.of(4.0));
    when(ratingRepo.countByBookId(1L)).thenReturn(10L);

    mockMvc.perform(get("/api/books/1/rating"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userRating").value(4))
        .andExpect(jsonPath("$.avgRating").value(4.0))
        .andExpect(jsonPath("$.ratingCount").value(10));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getRating_returnsZeroWhenNoUserRating() throws Exception {
    when(ratingRepo.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
    when(ratingRepo.findAvgRatingByBookId(1L)).thenReturn(Optional.empty());
    when(ratingRepo.countByBookId(1L)).thenReturn(0L);

    mockMvc.perform(get("/api/books/1/rating"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userRating").value(0))
        .andExpect(jsonPath("$.avgRating").value(0.0))
        .andExpect(jsonPath("$.ratingCount").value(0));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void submitRating_createsNewRating() throws Exception {
    when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
    when(ratingRepo.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
    when(ratingRepo.findAvgRatingByBookId(1L)).thenReturn(Optional.of(4.0));
    when(ratingRepo.countByBookId(1L)).thenReturn(1L);

    mockMvc.perform(post("/api/books/1/rating")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rating\": 4}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userRating").value(4));

    verify(ratingRepo).save(any());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void submitRating_updatesExistingRating() throws Exception {
    BookRating existing = BookRating.builder().user(user).book(book).rating((short) 3).build();

    when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
    when(ratingRepo.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(existing));
    when(ratingRepo.findAvgRatingByBookId(1L)).thenReturn(Optional.of(5.0));
    when(ratingRepo.countByBookId(1L)).thenReturn(1L);

    mockMvc.perform(post("/api/books/1/rating")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rating\": 5}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userRating").value(5));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void submitRating_returnsBadRequestWhenRatingTooLow() throws Exception {
    mockMvc.perform(post("/api/books/1/rating")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rating\": 0}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void submitRating_returnsBadRequestWhenRatingTooHigh() throws Exception {
    mockMvc.perform(post("/api/books/1/rating")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rating\": 6}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void deleteRating_deletesExistingRating() throws Exception {
    BookRating existing = BookRating.builder().user(user).book(book).rating((short) 3).build();

    when(ratingRepo.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(existing));

    mockMvc.perform(delete("/api/books/1/rating"))
        .andExpect(status().isNoContent());

    verify(ratingRepo).delete(existing);
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void deleteRating_succeedsWhenNoRatingExists() throws Exception {
    when(ratingRepo.findByUserIdAndBookId(anyLong(), anyLong())).thenReturn(Optional.empty());

    mockMvc.perform(delete("/api/books/1/rating"))
        .andExpect(status().isNoContent());
  }
}