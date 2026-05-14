package ru.goncharenko.notification.service.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.notification.mapper.NotificationMapper;
import ru.goncharenko.notification.model.entity.Notification;
import ru.goncharenko.notification.repository.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationJPA {
	private final NotificationRepository repository;
	private final NotificationMapper mapper;

	public Mono<Notification> saveNotificationMessage(Mono<NotificationDto> notification) {
		return notification.map(mapper::dtoToEntity)
				.flatMap(repository::save)
				.doOnSuccess(saved -> log.info("Saved: {}", saved))
				.doOnError(error -> log.error("Save failed", error));
	}
}
