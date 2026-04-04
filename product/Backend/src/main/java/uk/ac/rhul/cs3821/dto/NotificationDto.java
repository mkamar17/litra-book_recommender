package uk.ac.rhul.cs3821.dto;

import java.time.LocalDateTime;
import uk.ac.rhul.cs3821.model.Notification;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.NotificationType;

/**
 * Data transfer object for returning notification data in API responses.
 * Replaces the full Notification entity to avoid exposing recipient user data.
 */
public class NotificationDto {
  public Long id;
  public NotificationType type;
  public Long referenceId;
  public boolean read;
  public LocalDateTime createdAt;
  public String triggererName;

  /**
   * Constructs a NotificationDto with all fields.
   *
   * @param id            the notification ID
   * @param type          the notification type
   * @param referenceId   the ID of the related entity
   * @param read          whether the notification has been read
   * @param createdAt     the creation timestamp
   * @param triggererName the display name of the user who triggered the notification
   */
  public NotificationDto(Long id, NotificationType type, Long referenceId,
                         boolean read, LocalDateTime createdAt, String triggererName) {
    this.id = id;
    this.type = type;
    this.referenceId = referenceId;
    this.read = read;
    this.createdAt = createdAt;
    this.triggererName = triggererName;
  }

  /**
   * Converts a Notification entity to a NotificationDto.
   *
   * @param n         the notification entity to convert
   * @param triggerer the user who triggered the notification, or null if system-generated
   * @return the mapped {@code NotificationDto}
   */
  public static NotificationDto from(Notification n, User triggerer) {
    String name = triggerer != null
        ? (triggerer.getUsername() != null ? triggerer.getUsername() : triggerer.getEmail())
        : null;

    return new NotificationDto(
        n.getId(),
        n.getType(),
        n.getReferenceId(),
        n.isRead(),
        n.getCreatedAt(),
        name
    );
  }
}