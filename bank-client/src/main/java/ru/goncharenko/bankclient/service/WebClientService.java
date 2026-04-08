package ru.goncharenko.bankclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(WebClient.class)
public class WebClientService {
	@Qualifier("serviceWebClient")
	private final WebClient webClient;

	public <T, R> Mono<T> postForObject(String url, R requestBody, Class<T> responseType) {
		return webClient.post()
				.uri(url)
				.bodyValue(requestBody)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (response) ->
						response.bodyToMono(String.class).flatMap(error -> {
							log.error("Custom 4xx handler: {}", error);
							return Mono.error(new ResponseStatusException(
									HttpStatus.BAD_REQUEST,
									error
							));
						}))
				.bodyToMono(responseType);
	}
}
