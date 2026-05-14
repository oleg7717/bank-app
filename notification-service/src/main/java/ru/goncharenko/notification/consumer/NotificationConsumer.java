package ru.goncharenko.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.notification.service.jpa.NotificationJPA;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {
	private final NotificationJPA notificationJPA;

	@KafkaListener(topics = "notifications", groupId = "notification-consumer")
	public void processMessage(ConsumerRecord<String, NotificationDto> record) {
		notificationJPA.saveNotificationMessage(Mono.just(record.value()))
				.subscribe(
						null,
						error -> log.error("Error while saving notification", error)
				);
	}
}
