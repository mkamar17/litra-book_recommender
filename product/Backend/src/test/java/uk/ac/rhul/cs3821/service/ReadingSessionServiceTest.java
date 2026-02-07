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
import uk.ac.rhul.cs3821.model.UserBookProgress;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import uk.ac.rhul.cs3821.repository.UserBookProgressRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingSessionServiceTest {

  @Mock
  private ReadingSessionRepository sessionRepo;

  @Mock
  private UserBookProgressRepository progressRepo;

  @Mock
  private GamificationService gamificationService;

  @InjectMocks
  private ReadingSessionService readingSessionService;

  private User user;
  private Book book;
  private ReadingSession session;
  private UserBookProgress progress;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);

    book = new Book();
    book.setId(2L);
    book.setTitle("Test Book");

    session = ReadingSession.builder()
        .id(10L)
        .user(user)
        .book(book)
        .startTime(Instant.now().minusSeconds(300))
        .build();

    progress = UserBookProgress.builder()
        .user(user)
        .book(book)
        .currentPage(50)
        .totalPages(200)
        .build();
  }

  @Test
  void startSession_createsNewSession_whenNoActiveSession() {

    when(sessionRepo.findByUserAndBookAndEndTimeIsNull(user, book))
        .thenReturn(Optional.empty());
    when(sessionRepo.save(any()))
        .thenAnswer(inv -> inv.getArgument(0));

    ReadingSession result =
        readingSessionService.startSession(user, book);

    assertNotNull(result.getStartTime());
    assertNull(result.getEndTime());
    assertEquals(user, result.getUser());
    assertEquals(book, result.getBook());
  }

  @Test
  void startSession_endsExistingSession_first() {

    when(sessionRepo.findByUserAndBookAndEndTimeIsNull(user, book))
        .thenReturn(Optional.of(session));
    when(sessionRepo.save(any()))
        .thenAnswer(inv -> inv.getArgument(0));

    ReadingSession result =
        readingSessionService.startSession(user, book);

    assertNotNull(session.getEndTime());
    assertNotNull(session.getDurationSeconds());
    assertEquals(user, result.getUser());
  }

  @Test
  void endSession_setsEndTimeAndDuration() {

    when(sessionRepo.save(any()))
        .thenAnswer(inv -> inv.getArgument(0));

    ReadingSession result =
        readingSessionService.endSession(session);

    assertNotNull(result.getEndTime());
    assertTrue(result.getDurationSeconds() > 0);
  }

  @Test
  void endSessionById_successfullyUpdatesProgressAndAwardsPoints() {

    when(sessionRepo.findById(10L))
        .thenReturn(Optional.of(session));
    when(progressRepo.findByUserAndBook(user, book))
        .thenReturn(Optional.of(progress));
    when(progressRepo.save(any()))
        .thenAnswer(inv -> inv.getArgument(0));
    when(sessionRepo.save(any()))
        .thenAnswer(inv -> inv.getArgument(0));
    when(gamificationService.awardPointsForSession(user, 30))
        .thenReturn(150);

    var result =
        readingSessionService.endSession(10L, user, 80);

    assertEquals(80, progress.getCurrentPage());
    assertEquals(150, result.get("pointsAwarded"));
    assertEquals(30, result.get("pagesRead"));
    assertEquals(book.getId(), result.get("bookId"));
  }

  @Test
  void endSessionById_throwsForbidden_whenUserNotOwner() {

    User otherUser = new User();
    otherUser.setId(99L);

    when(sessionRepo.findById(10L))
        .thenReturn(Optional.of(session));

    assertThrows(ResponseStatusException.class, () ->
        readingSessionService.endSession(10L, otherUser, 60)
    );
  }

  @Test
  void endSessionById_throws_whenPageGoesBackwards() {

    when(sessionRepo.findById(10L))
        .thenReturn(Optional.of(session));
    when(progressRepo.findByUserAndBook(user, book))
        .thenReturn(Optional.of(progress));

    assertThrows(IllegalArgumentException.class, () ->
        readingSessionService.endSession(10L, user, 40)
    );
  }

  @Test
  void endSessionById_throws_whenPageExceedsTotalPages() {

    when(sessionRepo.findById(10L))
        .thenReturn(Optional.of(session));
    when(progressRepo.findByUserAndBook(user, book))
        .thenReturn(Optional.of(progress));

    assertThrows(IllegalArgumentException.class, () ->
        readingSessionService.endSession(10L, user, 250)
    );
  }

  @Test
  void getProgress_returnsOptional() {

    when(progressRepo.findByUserAndBook(user, book))
        .thenReturn(Optional.of(progress));

    Optional<UserBookProgress> result =
        readingSessionService.getProgress(user, book);

    assertTrue(result.isPresent());
  }
}
