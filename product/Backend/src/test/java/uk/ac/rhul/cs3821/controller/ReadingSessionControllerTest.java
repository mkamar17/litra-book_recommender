package uk.ac.rhul.cs3821.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

  private User user;
  private Book book;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    book = new Book();
    book.setId(1L);

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void startSession_withExistingProgress() throws Exception {

    ReadingSession session = ReadingSession.builder()
        .id(10L)
        .user(user)
        .book(book)
        .startTime(Instant.now())
        .build();

    UserBookProgress progress = UserBookProgress.builder()
        .currentPage(25)
        .build();

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(readingSessionService.getProgress(user, book))
        .thenReturn(Optional.of(progress));
    when(readingSessionService.startSession(user, book))
        .thenReturn(session);

    mockMvc.perform(post("/api/reading-sessions/start/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value(10))
        .andExpect(jsonPath("$.hasProgress").value(true))
        .andExpect(jsonPath("$.currentPage").value(25));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void startSession_withoutProgress() throws Exception {

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(readingSessionService.getProgress(user, book))
        .thenReturn(Optional.empty());
    when(readingSessionService.startSession(user, book))
        .thenReturn(ReadingSession.builder().id(11L).build());

    mockMvc.perform(post("/api/reading-sessions/start/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasProgress").value(false))
        .andExpect(jsonPath("$.currentPage").value(0));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void endSession_success() throws Exception {

    when(readingSessionService.endSession(1L, user, 50))
        .thenReturn(Map.of("ended", true));

    mockMvc.perform(
            post("/api/reading-sessions/end/{sessionId}", 1L)
                .param("pageReached", "50"))
        .andExpect(status().isOk());

    verify(readingSessionService)
        .endSession(1L, user, 50);
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createProgress_whenNoneExists() throws Exception {

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(progressRepository.findByUserAndBook(user, book))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            post("/api/reading-sessions/progress/{bookId}", 1L)
                .param("totalPages", "300"))
        .andExpect(status().isOk());

    verify(progressRepository).save(any(UserBookProgress.class));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createProgress_whenAlreadyExists() throws Exception {

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(progressRepository.findByUserAndBook(user, book))
        .thenReturn(Optional.of(new UserBookProgress()));

    mockMvc.perform(
            post("/api/reading-sessions/progress/{bookId}", 1L)
                .param("totalPages", "300"))
        .andExpect(status().isOk());

    verify(progressRepository, never()).save(any());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getProgress_whenExists() throws Exception {

    UserBookProgress progress = UserBookProgress.builder()
        .currentPage(40)
        .totalPages(200)
        .build();

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(readingSessionService.getProgress(user, book))
        .thenReturn(Optional.of(progress));

    mockMvc.perform(get("/api/reading-sessions/progress/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.current_page").value(40))
        .andExpect(jsonPath("$.total_pages").value(200));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getProgress_whenMissing() throws Exception {

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(readingSessionService.getProgress(user, book))
        .thenReturn(Optional.empty());

    mockMvc.perform(get("/api/reading-sessions/progress/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.current_page").value(0))
        .andExpect(jsonPath("$.total_pages").value(0));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void getAllProgress_returnsList() throws Exception {

    when(progressRepository.findByUser(user))
        .thenReturn(List.of(new UserBookProgress()));

    mockMvc.perform(get("/api/reading-sessions/progress"))
        .andExpect(status().isOk());
  }
}
