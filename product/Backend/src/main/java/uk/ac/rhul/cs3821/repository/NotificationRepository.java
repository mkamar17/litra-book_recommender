package uk.ac.rhul.cs3821.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Notification;

/**
 * Repository for Notification entities.
 * Provides queries for retrieving and counting user notifications.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /**
   * Returns all notifications for a user, ordered by creation time descending.
   *
   * @param recipientId the ID of the recipient user
   * @return list of Notification entities, newest first
   */
  List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

  /**
   * Returns all unread notifications for a user.
   *
   * @param recipientId the ID of the recipient user
   * @return list of unread Notification entities
   */
  List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

  /**
   * Returns the count of unread notifications for a user.
   * Used to drive the notification badge number on the frontend.
   *
   * @param recipientId the ID of the recipient user
   * @return the number of unread notifications
   */
  int countByRecipientIdAndReadFalse(Long recipientId);
}