package uk.ac.rhul.cs3821.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.UserBookProgress;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;
import uk.ac.rhul.cs3821.repository.UserBookProgressRepository;

/**
 * Service responsible for managing reading session logic.
 * Handles starting and ending reading sessions as well as calculating session duration.
 */

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

  private final ReadingSessionRepository sessionRepo;
  private final UserBookProgressRepository progressRepo;
  private final GamificationService gamificationService;
  private final LeaderboardService leaderboardService;

  /**
   * Method handles starting new reading session.
   *
   * @param user to represent user reading book.
   * @param book to represent the book which is being read.
   * @return the newly created reading session.
   */
  public ReadingSession startSession(User user, Book book) {

    // to end any existing active reading session
    sessionRepo
        .findByUserAndBookAndEndTimeIsNull(user, book)
        .ifPresent(this::endSession);

    ReadingSession session = ReadingSession.builder()
        .user(user)
        .book(book)
        .startTime(Instant.now())
        .build();

    return sessionRepo.save(session);
  }

  /**
   * Ends the given reading session and calculates its duration.
   * The session duration is calculated as the difference between the start time and the current time.
   *
   * @param session the reading session to end
   * @return the updated reading session
   */

  public ReadingSession endSession(ReadingSession session) {
    Instant end = Instant.now();

    session.setEndTime(end);
    session.setDurationSeconds(
        Duration.between(session.getStartTime(), end).getSeconds()
    );

    return sessionRepo.save(session);
  }

  /**
   * Small wrapper method that accepts sessionID and user.
   *
   * @param sessionId accepted
   * @param user      to represent the authenticated user
   * @return the session
   */
  public Map<String, Object> endSession(Long sessionId, User user, int pageReached) {
    ReadingSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!session.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    Book book = session.getBook();

    UserBookProgress progress = progressRepo
        .findByUserAndBook(user, book)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "No progress record found for this book"));

    if (pageReached < progress.getCurrentPage()) {
      throw new IllegalArgumentException("Page cannot go backwards");
    }

    if (pageReached > progress.getTotalPages()) {
      throw new IllegalArgumentException("Page exceeds total pages");
    }

    int pagesReadThisSession = pageReached - progress.getCurrentPage();

    progress.setCurrentPage(pageReached);
    progressRepo.save(progress);

    boolean bookCompleted = pageReached >= progress.getTotalPages();
    int pointsAwarded = gamificationService.awardPointsForSession(user, pagesReadThisSession, bookCompleted);
    leaderboardService.recomputeForUser(user);

    ReadingSession endedSession = endSession(session);

    // updating the reading streak if needed (-1 if session too short, >0 means streak updated)

    int streakDays = gamificationService.updateStreak(user, endedSession.getDurationSeconds());

    return Map.of(
        "sessionId", endedSession.getId(),
        "pointsAwarded", pointsAwarded,
        "pagesRead", pagesReadThisSession,
        "durationSeconds", endedSession.getDurationSeconds(),
        "bookId", book.getId(),
        "bookTitle", book.getTitle(),
        "currentPage", pageReached,
        "totalPages", progress.getTotalPages(),
        "streakDays", streakDays
    );
  }

  /**
   * Checking if progress exists.
   *
   * @param user to represent user.
   * @param book to represent book.
   * @return an Optional containing the user's progress if found.
   */
  public Optional<UserBookProgress> getProgress(User user, Book book) {
    return progressRepo.findByUserAndBook(user, book);
  }

}
