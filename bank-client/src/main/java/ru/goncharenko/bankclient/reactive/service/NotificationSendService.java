package ru.goncharenko.bankclient.reactive.service;

import reactor.core.publisher.Mono;

public interface NotificationSendService {
	Mono<Void> sendNotification(String service, String message);
}
