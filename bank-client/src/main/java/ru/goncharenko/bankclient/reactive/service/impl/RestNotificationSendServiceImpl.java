package ru.goncharenko.bankclient.reactive.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.NotificationDto;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.service.WebClientService;

import java.time.LocalDateTime;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.NOTIFICATION_BASE_URL;

@Slf4j
@Service
@ConditionalOnClass(WebClient.class)
@ConditionalOnProperty(name = "${application.service.notification.communication-method}", value = "rest", matchIfMissing = true)
public class RestNotificationSendServiceImpl implements NotificationSendService {
	private final String notificationUrl;
	private final WebClientService webClientService;

	public RestNotificationSendServiceImpl(@Value("${application.service.notification.url:http://notification-service}") String notificationBaseUrl,
	                                       final WebClientService webClientService) {
		this.notificationUrl = notificationBaseUrl + NOTIFICATION_BASE_URL;
		this.webClientService = webClientService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Mono<Void> sendNotification(String service, String message) {
		NotificationDto notification = NotificationDto.builder()
				.service(service)
				.message(message)
				.created(LocalDateTime.now())
				.build();

		return webClientService.postForObject(notificationUrl, service, notification, Void.class);
	}
}
