package ru.goncharenko.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.notification.model.entity.Notification;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.notification.service.jpa.NotificationJPA;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationJPA notificationJPA;

	@PreAuthorize("hasRole('notification')")
	public Mono<Notification> saveNotificationMessage(Mono<NotificationDto> notification) {
		return notificationJPA.saveNotificationMessage(notification);
	}
}
