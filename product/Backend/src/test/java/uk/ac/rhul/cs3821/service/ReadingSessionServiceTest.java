//package uk.ac.rhul.cs3821.service;
//
//import java.time.Instant;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.server.ResponseStatusException;
//import uk.ac.rhul.cs3821.model.Book;
//import uk.ac.rhul.cs3821.model.ReadingSession;
//import uk.ac.rhul.cs3821.model.User;
//import uk.ac.rhul.cs3821.model.UserBookProgress;
//import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
//import uk.ac.rhul.cs3821.repository.UserBookProgressRepository;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ReadingSessionServiceTest {
//
//  @Mock
//  private ReadingSessionRepository sessionRepository;
//
//  @Mock
//  private UserBookProgressRepository progressRepository;
//
//  @InjectMocks
//  private ReadingSessionService readingSessionService;
//
//  private User user;
//  private Book book;
//
//  @BeforeEach
//  void setUp() {
//    user = new User();
//    user.setId(1L);
//
//    book = new Book();
//    book.setId(2L);
//  }
//
//  private ReadingSession createSession() {
//    return ReadingSession.builder()
//        .id(10L)
//        .user(user)
//        .book(book)
//        .startTime(Instant.now().minusSeconds(300))
//        .build();
//  }
//
//  private UserBookProgress createProgress(int currentPage, int totalPages) {
//    return UserBookProgress.builder()
//        .user(user)
//        .book(book)
//        .currentPage(currentPage)
//        .totalPages(totalPages)
//        .build();
//  }
//
//  private void mockFindSessionAndProgress(
//      ReadingSession session,
//      UserBookProgress progress
//  ) {
//    when(sessionRepository.findById(session.getId()))
//        .thenReturn(Optional.of(session));
//
//    when(progressRepository.findByUserAndBook(user, book))
//        .thenReturn(Optional.of(progress));
//  }
//
//  private void mockSaveSessionAndProgress() {
//    when(sessionRepository.save(any()))
//        .thenAnswer(invocation -> invocation.getArgument(0));
//
//    when(progressRepository.save(any()))
//        .thenAnswer(invocation -> invocation.getArgument(0));
//  }
//
//  @Test
//  void startSession_createsNewSession_whenNoActiveSessionExists() {
//    when(sessionRepository.findByUserAndBookAndEndTimeIsNull(user, book))
//        .thenReturn(Optional.empty());
//
//    when(sessionRepository.save(any()))
//        .thenAnswer(invocation -> invocation.getArgument(0));
//
//    ReadingSession result =
//        readingSessionService.startSession(user, book);
//
//    assertNotNull(result);
//    assertEquals(user, result.getUser());
//    assertEquals(book, result.getBook());
//    assertNotNull(result.getStartTime());
//    assertNull(result.getEndTime());
//    assertNull(result.getDurationSeconds());
//  }
//
//  @Test
//  void startSession_endsExistingActiveSession_beforeStartingNewOne() {
//    ReadingSession activeSession = createSession();
//
//    when(sessionRepository.findByUserAndBookAndEndTimeIsNull(user, book))
//        .thenReturn(Optional.of(activeSession));
//
//    when(sessionRepository.save(any()))
//        .thenAnswer(invocation -> invocation.getArgument(0));
//
//    ReadingSession newSession =
//        readingSessionService.startSession(user, book);
//
//    assertNotNull(activeSession.getEndTime());
//    assertNotNull(activeSession.getDurationSeconds());
//    assertEquals(user, newSession.getUser());
//  }
//
//  @Test
//  void endSession_setsEndTimeAndDuration() {
//    ReadingSession session = createSession();
//
//    when(sessionRepository.save(session)).thenReturn(session);
//
//    ReadingSession result =
//        readingSessionService.endSession(session);
//
//    assertNotNull(result.getEndTime());
//    assertNotNull(result.getDurationSeconds());
//    assertTrue(result.getDurationSeconds() >= 300);
//  }
//
//  @Test
//  void endSessionById_allowsOwner_andUpdatesProgress() {
//    ReadingSession session = createSession();
//    UserBookProgress progress = createProgress(50, 200);
//
//    mockFindSessionAndProgress(session, progress);
//    mockSaveSessionAndProgress();
//
//    ReadingSession result =
//        readingSessionService.endSession(10L, user, 80);
//
//    assertNotNull(result.getEndTime());
//    assertEquals(80, progress.getCurrentPage());
//  }
//
//  @Test
//  void endSessionById_throwsForbidden_whenUserIsNotOwner() {
//    User otherUser = new User();
//    otherUser.setId(99L);
//
//    ReadingSession session = createSession();
//
//    when(sessionRepository.findById(10L))
//        .thenReturn(Optional.of(session));
//
//    assertThrows(ResponseStatusException.class, () ->
//        readingSessionService.endSession(10L, otherUser, 20)
//    );
//  }
//
//  @Test
//  void endSession_throwsException_whenPageGoesBackwards() {
//    ReadingSession session = createSession();
//    UserBookProgress progress = createProgress(50, 200);
//
//    mockFindSessionAndProgress(session, progress);
//
//    assertThrows(IllegalArgumentException.class, () ->
//        readingSessionService.endSession(10L, user, 30)
//    );
//  }
//
//  @Test
//  void endSession_throwsException_whenPageExceedsTotalPages() {
//    ReadingSession session = createSession();
//    UserBookProgress progress = createProgress(150, 200);
//
//    mockFindSessionAndProgress(session, progress);
//
//    assertThrows(IllegalArgumentException.class, () ->
//        readingSessionService.endSession(10L, user, 250)
//    );
//  }
//}
