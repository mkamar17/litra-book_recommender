package uk.ac.rhul.cs3821.model;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReadingSessionTest {

  @Test
  void noArgsConstructor_createsEmptyObject() {
    ReadingSession session = new ReadingSession();

    assertNull(session.getId());
    assertNull(session.getUser());
    assertNull(session.getBook());
    assertNull(session.getStartTime());
    assertNull(session.getEndTime());
    assertNull(session.getDurationSeconds());
  }

  @Test
  void allArgsConstructor_setsAllFieldsCorrectly() {
    User user = new User();
    Book book = new Book();
    Instant start = Instant.now();
    Instant end = start.plusSeconds(3600);

    ReadingSession session = new ReadingSession(
        1L,
        user,
        book,
        start,
        end,
        3600L
    );

    assertEquals(1L, session.getId());
    assertEquals(user, session.getUser());
    assertEquals(book, session.getBook());
    assertEquals(start, session.getStartTime());
    assertEquals(end, session.getEndTime());
    assertEquals(3600L, session.getDurationSeconds());
  }

  @Test
  void builder_createsReadingSessionCorrectly() {
    User user = new User();
    Book book = new Book();
    Instant start = Instant.now();
    Instant end = start.plusSeconds(1800);

    ReadingSession session = ReadingSession.builder()
        .user(user)
        .book(book)
        .startTime(start)
        .endTime(end)
        .durationSeconds(1800L)
        .build();

    assertNull(session.getId()); // not set by builder
    assertEquals(user, session.getUser());
    assertEquals(book, session.getBook());
    assertEquals(start, session.getStartTime());
    assertEquals(end, session.getEndTime());
    assertEquals(1800L, session.getDurationSeconds());
  }

  @Test
  void setters_updateFieldsCorrectly() {
    ReadingSession session = new ReadingSession();

    User user = new User();
    Book book = new Book();
    Instant start = Instant.now();

    session.setUser(user);
    session.setBook(book);
    session.setStartTime(start);
    session.setDurationSeconds(600L);

    assertEquals(user, session.getUser());
    assertEquals(book, session.getBook());
    assertEquals(start, session.getStartTime());
    assertEquals(600L, session.getDurationSeconds());
  }

  @Test
  void endTime_canBeNull_forOngoingSession() {
    ReadingSession session = new ReadingSession();
    session.setStartTime(Instant.now());

    assertNull(session.getEndTime());
  }
}
