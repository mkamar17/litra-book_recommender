package uk.ac.rhul.cs3821.controller;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.UserBookProgress;
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.repository.UserBookProgressRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import uk.ac.rhul.cs3821.service.ReadingSessionService;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReadingSessionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReadingSessionService readingSessionService;

  @MockitoBean
  private BookRepository bookRepository;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private UserBookProgressRepository progressRepository;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AppUserDetailsService uds;

  private User createUser() {
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    return user;
  }

  private Book createBook(Long id) {
    Book book = new Book();
    book.setId(id);
    return book;
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void startSession_returnsSessionAndProgressInfo() throws Exception {
    User user = createUser();
    Book book = createBook(1L);

    ReadingSession session = ReadingSession.builder()
        .id(100L)
        .user(user)
        .book(book)
        .startTime(Instant.now())
        .build();

    UserBookProgress progress = UserBookProgress.builder()
        .user(user)
        .book(book)
        .currentPage(20)
        .build();

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(bookRepository.findById(1L))
        .thenReturn(Optional.of(book));
    when(readingSessionService.getProgress(user, book))
        .thenReturn(Optional.of(progress));
    when(readingSessionService.startSession(user, book))
        .thenReturn(session);

    mockMvc.perform(post("/api/reading-sessions/start/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value(100L))
        .andExpect(jsonPath("$.hasProgress").value(true))
        .andExpect(jsonPath("$.currentPage").value(20));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void endSession_success() throws Exception {
    User user = createUser();
    ReadingSession session = new ReadingSession();
    session.setId(1L);

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(readingSessionService.endSession(1L, user, 50))
        .thenReturn(session);

    mockMvc.perform(
            post("/api/reading-sessions/end/{sessionId}", 1L)
                .param("pageReached", "50")
        )
        .andExpect(status().isOk());

    verify(readingSessionService)
        .endSession(1L, user, 50);
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createProgress_createsNewProgress_whenNoneExists() throws Exception {
    User user = createUser();
    Book book = createBook(2L);

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(bookRepository.findById(2L))
        .thenReturn(Optional.of(book));
    when(progressRepository.findByUserAndBook(user, book))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            post("/api/reading-sessions/progress/{bookId}", 2L)
                .param("totalPages", "300")
        )
        .andExpect(status().isOk());

    verify(progressRepository).save(any(UserBookProgress.class));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createProgress_doesNothing_whenProgressAlreadyExists() throws Exception {
    User user = createUser();
    Book book = createBook(2L);

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(bookRepository.findById(2L))
        .thenReturn(Optional.of(book));
    when(progressRepository.findByUserAndBook(user, book))
        .thenReturn(Optional.of(new UserBookProgress()));

    mockMvc.perform(
            post("/api/reading-sessions/progress/{bookId}", 2L)
                .param("totalPages", "300")
        )
        .andExpect(status().isOk());

    verify(progressRepository, never()).save(any());
  }
}
