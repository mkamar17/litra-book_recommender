//package uk.ac.rhul.cs3821.service;
//
//import jakarta.persistence.EntityNotFoundException;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.access.AccessDeniedException;
//import uk.ac.rhul.cs3821.model.Notification;
//import uk.ac.rhul.cs3821.model.User;
//import uk.ac.rhul.cs3821.model.enums.NotificationType;
//import uk.ac.rhul.cs3821.repository.NotificationRepository;
//import uk.ac.rhul.cs3821.repository.UserRepository;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class NotificationServiceTest {
//
//  @Mock
//  private NotificationRepository notificationRepository;
//  @Mock
//  private UserRepository userRepository;
//
//  @InjectMocks
//  private NotificationService notificationService;
//
//  private User user;
//  private Notification unreadNotification;
//  private Notification readNotification;
//
//  @BeforeEach
//  void setUp() {
//    user = new User();
//    user.setId(1L);
//    user.setEmail("user@example.com");
//
//    unreadNotification = new Notification();
//    unreadNotification.setId(1L);
//    unreadNotification.setRecipient(user);
//    unreadNotification.setType(NotificationType.FRIEND_REQUEST);
//    unreadNotification.setReferenceId(10L);
//    unreadNotification.setRead(false);
//
//    readNotification = new Notification();
//    readNotification.setId(2L);
//    readNotification.setRecipient(user);
//    readNotification.setType(NotificationType.COMMENT_REPLY);
//    readNotification.setReferenceId(20L);
//    readNotification.setRead(true);
//  }
//
//  @Test
//  void send_savesNewNotification() {
//    when(userRepository.getReferenceById(1L)).thenReturn(user);
//
//    notificationService.send(1L, NotificationType.FRIEND_REQUEST, 10L);
//
//    verify(notificationRepository).save(any(Notification.class));
//  }
//
//  @Test
//  void getAllForUser_returnsAllNotifications() {
//    when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
//        .thenReturn(List.of(unreadNotification, readNotification));
//
//    List<Notification> result = notificationService.getAllForUser(1L);
//
//    assertEquals(2, result.size());
//  }
//
//  @Test
//  void getUnreadForUser_returnsOnlyUnread() {
//    when(notificationRepository.findByRecipientIdAndReadFalse(1L))
//        .thenReturn(List.of(unreadNotification));
//
//    List<Notification> result = notificationService.getUnreadForUser(1L);
//
//    assertEquals(1, result.size());
//    assertTrue(!result.get(0).isRead());
//  }
//
//  @Test
//  void getUnreadCount_returnsCorrectCount() {
//    when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(3);
//
//    assertEquals(3, notificationService.getUnreadCount(1L));
//  }
//
//  @Test
//  void markAllAsRead_marksAllUnreadNotifications() {
//    when(notificationRepository.findByRecipientIdAndReadFalse(1L))
//        .thenReturn(List.of(unreadNotification));
//
//    notificationService.markAllAsRead(1L);
//
//    assertTrue(unreadNotification.isRead());
//    verify(notificationRepository).saveAll(any());
//  }
//
//  @Test
//  void markAsRead_marksSingleNotification() throws Exception {
//    when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
//
//    notificationService.markAsRead(1L, 1L);
//
//    assertTrue(unreadNotification.isRead());
//    verify(notificationRepository).save(unreadNotification);
//  }
//
//  @Test
//  void markAsRead_throwsWhenNotOwner() {
//    when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
//
//    assertThrows(AccessDeniedException.class,
//        () -> notificationService.markAsRead(1L, 99L));
//  }
//
//  @Test
//  void markAsRead_throwsWhenNotFound() {
//    when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
//
//    assertThrows(EntityNotFoundException.class,
//        () -> notificationService.markAsRead(99L, 1L));
//  }
//}