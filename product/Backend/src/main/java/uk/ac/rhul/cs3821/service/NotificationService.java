package uk.ac.rhul.cs3821.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.rhul.cs3821.dto.NotificationDto;
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

  /**
   * Constructor with the following parameters.
   *
   * @param notificationRepository notificationRepository
   * @param userRepository         userRepository
   */
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
  public void send(Long recipientId, NotificationType type, Long referenceId, Long triggererId) {
    User recipient = userRepository.getReferenceById(recipientId);

    Notification notification = new Notification();
    notification.setRecipient(recipient);
    notification.setType(type);
    notification.setReferenceId(referenceId);
    notification.setTriggererId(triggererId);
    notification.setRead(false);

    notificationRepository.save(notification);
  }

  /**
   * Returns all notifications for a user, newest first.
   */
  @Transactional(readOnly = true)
  public List<NotificationDto> getAllForUser(Long userId) {
    return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
  }

  /**
   * Returns only unread notifications for a user.
   */
  @Transactional(readOnly = true)
  public List<NotificationDto> getUnreadForUser(Long userId) {
    return notificationRepository.findByRecipientIdAndReadFalse(userId).stream().map(this::toDto).toList();
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
  public void markAsRead(Long notificationId, Long currentUserId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

    // Security check — users can only mark their own notifications as read
    if (!notification.getRecipient().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Not your notification");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
  }

  /**
   * Helper method to link to Dto.
   *
   * @param n the notification
   * @return the full dto
   */
  private NotificationDto toDto(Notification n) {
    User triggerer = (n.getTriggererId() != null && n.getTriggererId() != 0)
        ? userRepository.findById(n.getTriggererId()).orElse(null)
        : null;
    return NotificationDto.from(n, triggerer);
  }
}