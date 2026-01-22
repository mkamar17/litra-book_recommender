package uk.ac.rhul.cs3821.service;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.rhul.cs3821.model.Book;
import uk.ac.rhul.cs3821.model.ReadingSession;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.ReadingSessionRepository;

/**
 * Service responsible for managing reading session logic.
 * Handles starting and ending reading sessions as well as calculating session duration.
 */

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

  private final ReadingSessionRepository sessionRepo;

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
  public ReadingSession endSession(Long sessionId, User user) {
    ReadingSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // 🔐 Authorization check
    if (!session.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    return endSession(session);
  }

}
