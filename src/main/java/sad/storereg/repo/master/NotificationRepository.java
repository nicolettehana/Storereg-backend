package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer>{

	Optional<Notification> findByMessageIdEquals(String messageId);

	List<Notification> findAll();
	
}
