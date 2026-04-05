package ru.goncharenko.notification.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.goncharenko.notification.model.entity.Notification;

public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {
}
