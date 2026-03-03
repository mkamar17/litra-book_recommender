package uk.ac.rhul.cs3821.controller;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.model.Notification;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.NotificationService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock
  private NotificationService notificationService;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationController controller;

  private User currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(currentUser));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getAllReturnsAllNotifications() {
    Notification n = new Notification();
    n.setRecipient(currentUser);
    n.setType(NotificationType.FRIEND_REQUEST);
    n.setRead(false);

    when(notificationService.getAllForUser(1L)).thenReturn(List.of(n));

    var response = controller.getAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getUnreadReturnsOnlyUnread() {
    Notification n = new Notification();
    n.setRecipient(currentUser);
    n.setType(NotificationType.COMMENT_REPLY);
    n.setRead(false);

    when(notificationService.getUnreadForUser(1L)).thenReturn(List.of(n));

    var response = controller.getUnread();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getUnreadCountReturnsBadgeNumber() {
    when(notificationService.getUnreadCount(1L)).thenReturn(3);

    var response = controller.getUnreadCount();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(3, response.getBody());
  }

  @Test
  void markAsReadReturns204() throws Exception {
    var response = controller.markAsRead(10L);

    verify(notificationService).markAsRead(10L, 1L);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void markAllAsReadReturns204() {
    var response = controller.markAllAsRead();

    verify(notificationService).markAllAsRead(1L);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void throwsIfUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        jakarta.persistence.EntityNotFoundException.class,
        () -> controller.getAll()
    );
  }
}