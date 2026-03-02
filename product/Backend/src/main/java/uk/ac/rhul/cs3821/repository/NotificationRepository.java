package uk.ac.rhul.cs3821.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

  List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

  int countByRecipientIdAndReadFalse(Long recipientId);
}