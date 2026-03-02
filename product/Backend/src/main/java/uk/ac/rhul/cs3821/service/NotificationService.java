package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.rhul.cs3821.model.Notification;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;
import uk.ac.rhul.cs3821.repository.NotificationRepository;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Service for managing user notifications.
 */
@Service
@Transactional
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  public NotificationService(NotificationRepository notificationRepository,
                             UserRepository userRepository) {
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
  }

  /**
   * Creates and saves a notification for a recipient.
   *
   * @param recipientId the user to notify
   * @param type        the type of notification
   * @param referenceId the ID of the related entity (friendshipId, commentId etc)
   */
  public void send(Long recipientId, NotificationType type, Long referenceId) {
    User recipient = userRepository.getReferenceById(recipientId);

    Notification notification = new Notification();
    notification.setRecipient(recipient);
    notification.setType(type);
    notification.setReferenceId(referenceId);
    notification.setRead(false);

    notificationRepository.save(notification);
  }

  /**
   * Returns all notifications for a user, newest first.
   */
  @Transactional(readOnly = true)
  public List<Notification> getAllForUser(Long userId) {
    return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
  }

  /**
   * Returns only unread notifications for a user.
   */
  @Transactional(readOnly = true)
  public List<Notification> getUnreadForUser(Long userId) {
    return notificationRepository.findByRecipientIdAndReadFalse(userId);
  }

  /**
   * Returns the count of unread notifications — used for the bell badge on the frontend.
   */
  @Transactional(readOnly = true)
  public int getUnreadCount(Long userId) {
    return notificationRepository.countByRecipientIdAndReadFalse(userId);
  }

  /**
   * Marks all of a user's notifications as read.
   */
  public void markAllAsRead(Long userId) {
    List<Notification> unread = notificationRepository
        .findByRecipientIdAndReadFalse(userId);

    unread.forEach(n -> n.setRead(true));
    notificationRepository.saveAll(unread);
  }

  /**
   * Marks a single notification as read.
   */
  public void markAsRead(Long notificationId, Long currentUserId) throws AccessDeniedException {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

    // Security check — users can only mark their own notifications as read
    if (!notification.getRecipient().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Not your notification");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
  }
}