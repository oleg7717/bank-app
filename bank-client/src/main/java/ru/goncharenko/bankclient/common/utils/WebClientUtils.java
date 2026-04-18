package ru.goncharenko.bankclient.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
public class WebClientUtils {
	public static boolean isServerUnavailable(HttpStatusCode status) {
		return status.isSameCodeAs(HttpStatus.REQUEST_TIMEOUT) ||
				status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS) ||
				status.isSameCodeAs(HttpStatus.BAD_GATEWAY) ||
				status.isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE) ||
				status.isSameCodeAs(HttpStatus.GATEWAY_TIMEOUT);
	}

	public static Mono<? extends Throwable> throwError(ClientResponse response,
	                                             BiFunction<String, Integer, RuntimeException> exception) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("{}")
				.flatMap(responseBody -> {
					int statusCode = response.statusCode().value();
					log.error("HTTP status code: {} , response body: {}", statusCode, responseBody);
					return Mono.error(exception.apply(responseBody, statusCode));
				});
	}
}
