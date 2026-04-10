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
import ru.goncharenko.bankclient.utils.BearerAuthResolver;

import static org.springframework.security.oauth2.client.web.ClientAttributes.clientRegistrationId;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(WebClient.class)
public class WebClientService {
	@Qualifier("serviceWebClient")
	private final WebClient webClient;

	public <T, R> Mono<T> postForObject(String url, String service, R requestBody, Class<T> responseType) {
		return webClient.post()
				.uri(url)
//				.headers(headers -> headers.setBearerAuth(token))
				.attributes(clientRegistrationId(service))
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
				.onStatus(HttpStatusCode::is5xxServerError, response ->
						response.bodyToMono(String.class).flatMap(error -> {
							log.error("Server error {}: {}", response.statusCode(), error);
							return Mono.error(new ResponseStatusException(
									response.statusCode(),
									"Server error: " + error
							));
						}))
				.bodyToMono(responseType);
	}
}
