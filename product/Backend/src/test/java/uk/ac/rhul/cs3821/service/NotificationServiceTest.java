package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import uk.ac.rhul.cs3821.dto.NotificationDto;
import uk.ac.rhul.cs3821.model.Notification;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.NotificationRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationService}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationService notificationService;

  private User user;
  private User triggerer;
  private Notification unreadNotification;
  private Notification readNotification;

  /**
   * Sets up a test user, triggerer, and notification fixtures.
   */
  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("user@example.com");

    triggerer = new User();
    triggerer.setId(99L);
    triggerer.setEmail("triggerer@example.com");

    unreadNotification = new Notification();
    unreadNotification.setId(1L);
    unreadNotification.setRecipient(user);
    unreadNotification.setType(NotificationType.FRIEND_REQUEST);
    unreadNotification.setReferenceId(10L);
    unreadNotification.setTriggererId(99L);
    unreadNotification.setRead(false);

    readNotification = new Notification();
    readNotification.setId(2L);
    readNotification.setRecipient(user);
    readNotification.setType(NotificationType.COMMENT_REPLY);
    readNotification.setReferenceId(20L);
    readNotification.setTriggererId(99L);
    readNotification.setRead(true);

    when(userRepository.findById(99L)).thenReturn(Optional.of(triggerer));
  }

  @Test
  void send_savesNewNotification() {
    when(userRepository.getReferenceById(1L)).thenReturn(user);

    notificationService.send(1L, NotificationType.FRIEND_REQUEST, 10L, 99L);

    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  void getAllForUser_returnsAllNotifications() {
    when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(unreadNotification, readNotification));

    List<NotificationDto> result = notificationService.getAllForUser(1L);

    assertEquals(2, result.size());
    assertEquals(NotificationType.FRIEND_REQUEST, result.get(0).type);
    assertEquals(NotificationType.COMMENT_REPLY, result.get(1).type);
  }

  @Test
  void getUnreadForUser_returnsOnlyUnread() {
    when(notificationRepository.findByRecipientIdAndReadFalse(1L))
        .thenReturn(List.of(unreadNotification));

    List<NotificationDto> result = notificationService.getUnreadForUser(1L);

    assertEquals(1, result.size());
    assertFalse(result.get(0).read);
  }

  @Test
  void getUnreadCount_returnsCorrectCount() {
    when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(3);

    assertEquals(3, notificationService.getUnreadCount(1L));
  }

  @Test
  void markAllAsRead_marksAllUnreadNotifications() {
    when(notificationRepository.findByRecipientIdAndReadFalse(1L))
        .thenReturn(List.of(unreadNotification));

    notificationService.markAllAsRead(1L);

    assertTrue(unreadNotification.isRead());
    verify(notificationRepository).saveAll(any());
  }

  @Test
  void markAsRead_marksSingleNotification() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));

    notificationService.markAsRead(1L, 1L);

    assertTrue(unreadNotification.isRead());
    verify(notificationRepository).save(unreadNotification);
  }

  @Test
  void markAsRead_throwsWhenNotOwner() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));

    assertThrows(AccessDeniedException.class,
        () -> notificationService.markAsRead(1L, 99L));
  }

  @Test
  void markAsRead_throwsWhenNotFound() {
    when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
        () -> notificationService.markAsRead(99L, 1L));
  }
}