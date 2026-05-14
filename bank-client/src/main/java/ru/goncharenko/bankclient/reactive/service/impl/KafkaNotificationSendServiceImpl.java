package ru.goncharenko.bankclient.reactive.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.bankclient.reactive.config.ReactiveKafkaProducer;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.service.notification.communication-method", havingValue = "kafka")
public class KafkaNotificationSendServiceImpl implements NotificationSendService {
	private final ReactiveKafkaProducer<String, NotificationDto> producer;

	@Override
	public Mono<Void> sendNotification(String service, String message) {
		NotificationDto notification = NotificationDto.builder()
				.service(service)
				.message(message)
				.created(LocalDateTime.now())
				.build();

		return producer.send("notifications", service, notification)
				.doOnError(error -> log.error("Failed to send message to Kafka", error))
				.doOnSuccess(success -> log.info("Send operation completed"))
				.then();
	}
}
