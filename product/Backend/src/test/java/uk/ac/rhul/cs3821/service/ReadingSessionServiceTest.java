package uk.ac.rhul.cs3821.service;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingSessionServiceTest {

  @Mock
  private ReadingSessionRepository sessionRepository;

  @InjectMocks
  private ReadingSessionService readingSessionService;

  private User user;
  private Book book;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);

    book = new Book();
    book.setId(2L);
  }

  @Test
  void startSession_createsNewSession_whenNoActiveSessionExists() {

    when(sessionRepository.findByUserAndBookAndEndTimeIsNull(user, book))
        .thenReturn(Optional.empty());

    when(sessionRepository.save(any(ReadingSession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReadingSession result = readingSessionService.startSession(user, book);

    assertNotNull(result);
    assertEquals(user, result.getUser());
    assertEquals(book, result.getBook());
    assertNotNull(result.getStartTime());
    assertNull(result.getEndTime());
    assertNull(result.getDurationSeconds());

    verify(sessionRepository).save(any(ReadingSession.class));
  }

  @Test
  void startSession_endsExistingActiveSession_beforeStartingNewOne() {

    ReadingSession activeSession = ReadingSession.builder()
        .user(user)
        .book(book)
        .startTime(Instant.now().minusSeconds(300))
        .build();

    when(sessionRepository.findByUserAndBookAndEndTimeIsNull(user, book))
        .thenReturn(Optional.of(activeSession));

    when(sessionRepository.save(any(ReadingSession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReadingSession newSession = readingSessionService.startSession(user, book);

    assertNotNull(activeSession.getEndTime());
    assertNotNull(activeSession.getDurationSeconds());

    assertEquals(user, newSession.getUser());
    assertEquals(book, newSession.getBook());

    verify(sessionRepository, times(2)).save(any(ReadingSession.class));
  }

  @Test
  void endSession_setsEndTimeAndDuration_andSavesSession() {

    Instant startTime = Instant.now().minusSeconds(600);

    ReadingSession session = ReadingSession.builder()
        .user(user)
        .book(book)
        .startTime(startTime)
        .build();

    when(sessionRepository.save(session)).thenReturn(session);

    ReadingSession result = readingSessionService.endSession(session);

    assertNotNull(result.getEndTime());
    assertNotNull(result.getDurationSeconds());
    assertTrue(result.getDurationSeconds() >= 600);

    verify(sessionRepository).save(session);
  }

  /**
   * Testing that the owner of the reading session can end the session.
   */
  @Test
  void endSessionById_allowsOwner() {
    ReadingSession session = ReadingSession.builder()
        .id(10L)
        .user(user)
        .book(book)
        .startTime(Instant.now().minusSeconds(300))
        .build();

    when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
    when(sessionRepository.save(any())).thenReturn(session);

    ReadingSession result =
        readingSessionService.endSession(10L, user);

    assertNotNull(result.getEndTime());
    assertNotNull(result.getDurationSeconds());
  }

  /**
   * Testing that unauthorised user cannot end reading session.
   */
  @Test
  void endSessionById_throwsForbidden_whenUserIsNotOwner() {
    User otherUser = new User();
    otherUser.setId(99L);

    ReadingSession session = ReadingSession.builder()
        .id(10L)
        .user(user) // owned by original user
        .book(book)
        .startTime(Instant.now().minusSeconds(300))
        .build();

    when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

    assertThrows(ResponseStatusException.class, () ->
        readingSessionService.endSession(10L, otherUser)
    );
  }


}
