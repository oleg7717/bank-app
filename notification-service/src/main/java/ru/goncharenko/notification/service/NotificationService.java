package ru.goncharenko.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.notification.mapper.NotificationMapper;
import ru.goncharenko.notification.model.entity.Notification;
import ru.goncharenko.notification.repository.NotificationRepository;
import ru.goncharenko.bankclient.model.NotificationDto;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository repository;
	private final NotificationMapper mapper;

	@PreAuthorize("hasRole('notification')")
	public Mono<Notification> saveNotificationMessage(Mono<NotificationDto> notification) {
		return notification.map(mapper::dtoToEntity).flatMap(repository::save);
	}
}
