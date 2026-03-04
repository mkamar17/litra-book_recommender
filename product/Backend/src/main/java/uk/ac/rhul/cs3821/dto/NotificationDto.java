package uk.ac.rhul.cs3821.dto;

import java.time.LocalDateTime;
import uk.ac.rhul.cs3821.model.Notification;
import uk.ac.rhul.cs3821.model.enums.NotificationType;

public class NotificationDto {
  public Long id;
  public NotificationType type;
  public Long referenceId;
  public boolean read;
  public LocalDateTime createdAt;

  public NotificationDto(Long id, NotificationType type, Long referenceId,
                         boolean read, LocalDateTime createdAt) {
    this.id = id;
    this.type = type;
    this.referenceId = referenceId;
    this.read = read;
    this.createdAt = createdAt;
  }

  public static NotificationDto from(Notification n) {
    return new NotificationDto(n.getId(), n.getType(), n.getReferenceId(),
        n.isRead(), n.getCreatedAt());
  }
}