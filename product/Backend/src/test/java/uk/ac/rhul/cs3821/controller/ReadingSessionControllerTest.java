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
import uk.ac.rhul.cs3821.repository.BookRepository;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import uk.ac.rhul.cs3821.service.ReadingSessionService;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingSessionController.class)
@AutoConfigureMockMvc(addFilters = false) // disables JWT/security filters
class ReadingSessionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReadingSessionService readingSessionService;

  @MockitoBean
  private BookRepository bookRepository;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AppUserDetailsService uds;

  @Test
  @WithMockUser
  void startSession_returnsCreatedReadingSession() throws Exception {

    Long bookId = 1L;
    Book book = new Book();
    book.setId(bookId);

    User user = new User();
    user.setId(10L);

    ReadingSession session = ReadingSession.builder()
        .id(100L)
        .user(user)
        .book(book)
        .startTime(Instant.now())
        .build();

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(readingSessionService.startSession(any(), eq(book)))
        .thenReturn(session);

    mockMvc.perform(post("/api/reading-sessions/start/{bookId}", bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100L));

    verify(bookRepository).findById(bookId);
    verify(readingSessionService).startSession(any(), eq(book));
  }

  @Test
  void startSession_throwsException_whenBookDoesNotExist() throws Exception {

    Long bookId = 99L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());


    mockMvc.perform(post("/api/reading-sessions/start/{bookId}", bookId))
        .andExpect(status().isNotFound());

    verify(bookRepository).findById(bookId);
    verifyNoInteractions(readingSessionService);
  }

  @Test
  @WithMockUser
  void endSession_success() throws Exception {
    ReadingSession session = new ReadingSession();
    session.setId(1L);

    when(readingSessionService.endSession(eq(1L), any()))
        .thenReturn(session);

    mockMvc.perform(post("/api/reading-sessions/end/{id}", 1L))
        .andExpect(status().isOk());

    verify(readingSessionService)
        .endSession(eq(1L), any());
  }

}
