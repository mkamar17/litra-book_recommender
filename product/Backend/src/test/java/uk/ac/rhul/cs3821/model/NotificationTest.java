package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationTest {

  private User recipient;
  private Notification notification;

  @BeforeEach
  void setUp() {
    recipient = new User();
    recipient.setId(1L);
    recipient.setEmail("user@example.com");

    notification = new Notification();
    notification.setRecipient(recipient);
    notification.setType(NotificationType.FRIEND_REQUEST);
    notification.setReferenceId(10L);
    notification.setRead(false);
  }

  @Test
  void settersAndGettersWork() {
    assertEquals(recipient, notification.getRecipient());
    assertEquals(NotificationType.FRIEND_REQUEST, notification.getType());
    assertEquals(10L, notification.getReferenceId());
    assertFalse(notification.isRead());
  }

  @Test
  void canBeMarkedAsRead() {
    notification.setRead(true);
    assertTrue(notification.isRead());
  }

  @Test
  void typeCanBeCommentReply() {
    notification.setType(NotificationType.COMMENT_REPLY);
    assertEquals(NotificationType.COMMENT_REPLY, notification.getType());
  }

  @Test
  void typeCanBeMilestone() {
    notification.setType(NotificationType.MILESTONE);
    assertEquals(NotificationType.MILESTONE, notification.getType());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    Notification empty = new Notification();
    assertNull(empty.getId());
    assertNull(empty.getRecipient());
    assertNull(empty.getType());
    assertNull(empty.getReferenceId());
    assertFalse(empty.isRead());
  }
}