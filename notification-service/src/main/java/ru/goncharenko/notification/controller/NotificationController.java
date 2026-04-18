package ru.goncharenko.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.notification.model.entity.Notification;
import ru.goncharenko.notification.service.NotificationService;
import ru.goncharenko.bankclient.common.model.NotificationDto;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.NOTIFICATION_BASE_URL;

@RestController
@RequestMapping(NOTIFICATION_BASE_URL)
@RequiredArgsConstructor
public class NotificationController {
	private final NotificationService service;

	@PostMapping
	public Mono<Notification> saveNotification(@RequestBody Mono<NotificationDto> notification) {
		return service.saveNotificationMessage(notification);
	}
}
