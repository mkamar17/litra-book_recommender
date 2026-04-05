package uk.ac.rhul.cs3821.controller;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.dto.NotificationDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.NotificationService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationController}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock
  private NotificationService notificationService;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationController controller;

  private User currentUser;
  private NotificationDto unreadDto;
  private NotificationDto readDto;

  /**
   * Sets up a test user, DTOs, and populates the security context.
   */
  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");

    unreadDto = new NotificationDto(1L, NotificationType.FRIEND_REQUEST, 10L, false, null, null);
    readDto = new NotificationDto(2L, NotificationType.COMMENT_REPLY, 20L, true, null, null);

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("test@example.com", null));

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(currentUser));
  }

  /**
   * Clears the security context after each test.
   */
  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getAllReturnsAllNotifications() {
    when(notificationService.getAllForUser(1L)).thenReturn(List.of(unreadDto, readDto));

    var response = controller.getAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
  }

  @Test
  void getUnreadReturnsOnlyUnread() {
    when(notificationService.getUnreadForUser(1L)).thenReturn(List.of(unreadDto));

    var response = controller.getUnread();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertFalse(response.getBody().get(0).read);
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

    Assertions.assertThrows(
        jakarta.persistence.EntityNotFoundException.class,
        () -> controller.getAll()
    );
  }
}