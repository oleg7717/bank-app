package ru.goncharenko.bankclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.NotificationDto;

import java.time.LocalDateTime;

import static ru.goncharenko.bankclient.endpoint.Endpoints.NOTIFICATION_BASE_URL;

@Slf4j
@Service
@ConditionalOnClass(WebClient.class)
public class NotificationSendService {
	private final String notificationUrl;
	private final WebClient webClient;

	public NotificationSendService(@Value("${application.service.notification.url:http://localhost:8084}") String notificationBaseUrl,
	                        final WebClient webClient) {
		this.notificationUrl = notificationBaseUrl + NOTIFICATION_BASE_URL;
		this.webClient = webClient;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Mono<ResponseEntity<Void>> sendNotification(String service, String message) {
		NotificationDto notification = NotificationDto.builder()
				.service(service)
				.message(message)
				.created(LocalDateTime.now())
				.build();

		return webClient.post()
				.uri(notificationUrl)
				.bodyValue(notification)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (response) ->
						response.bodyToMono(String.class).flatMap(error -> {
							log.error("Custom 4xx handler: {}", error);
							return Mono.error(new ResponseStatusException(
									HttpStatus.BAD_REQUEST,
									error
							));
						}))
				.toBodilessEntity();
	}
}
