package ru.goncharenko.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.notification.service.jpa.NotificationJPA;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {
	private final ObjectMapper objectMapper;
	private final NotificationJPA notificationJPA;

	@KafkaListener(topics = "notifications", groupId = "notification-consumer")
	public void processMessage(ConsumerRecord<String, byte[]> record) {
		var dto = objectMapper.readValue(record.value(), NotificationDto.class);
		notificationJPA.saveNotificationMessage(Mono.just(dto))
				.subscribe(
						null,
						error -> log.error("Error while saving notification", error)
				);
	}
}
