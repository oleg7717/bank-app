package ru.goncharenko.bankclient.reactive.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "${application.service.notification.communication-method}", value = "kafka")
public class KafkaNotificationSendServiceImpl implements NotificationSendService {
	@Override
	public Mono<Void> sendNotification(String service, String message) {
		NotificationDto notification = NotificationDto.builder()
				.service(service)
				.message(message)
				.created(LocalDateTime.now())
				.build();

		return null;
	}
}
