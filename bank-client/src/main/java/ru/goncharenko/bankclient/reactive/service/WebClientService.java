package ru.goncharenko.bankclient.reactive.service;

import reactor.core.publisher.Mono;

public interface WebClientService {
	<T, R> Mono<T> postForObject(String url, String service, R requestBody, Class<T> responseType);
}
