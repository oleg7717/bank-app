package ru.goncharenko.bankclient.reactive.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.goncharenko.bankclient.common.exception.ExternalServiceUnavailable;
import ru.goncharenko.bankclient.common.exception.HttpClientException;
import ru.goncharenko.bankclient.common.exception.ReceiverUnavailableException;
import ru.goncharenko.bankclient.common.utils.WebClientUtils;
import ru.goncharenko.bankclient.reactive.service.WebClientService;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.security.oauth2.client.web.ClientAttributes.clientRegistrationId;
import static ru.goncharenko.bankclient.common.utils.WebClientUtils.throwError;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(WebClient.class)
public class WebClientServiceImpl implements WebClientService {
	private final MeterRegistry meterRegistry;

	@Qualifier("serviceWebClient")
	private final WebClient webClient;

	private final Map<String, Counter> retryCounters = new ConcurrentHashMap<>();

	public <T, R> Mono<T> postForObject(String url, String service, R requestBody, Class<T> responseType) {
		int maxAttempts = 5;
		Counter retryCounter = retryCounters.computeIfAbsent(service,
				s -> Counter.builder("failed_retry_count")
						.tag("service", s)
						.register(meterRegistry)
		);

		return webClient.post()
				.uri(url)
				.attributes(clientRegistrationId(service))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(requestBody)
				.retrieve()
				.onStatus(
						WebClientUtils::isServerUnavailable,
						response -> throwError(response, ReceiverUnavailableException::new)
				)
				.onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
						response -> throwError(response, HttpClientException::new)
				)
				.bodyToMono(responseType)
				.retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(1000))
						.filter(throwable -> throwable instanceof ReceiverUnavailableException || throwable instanceof WebClientRequestException)
						.doAfterRetry(retrySignal -> {
							log.warn("Retry {} from total retries {}", retrySignal.totalRetriesInARow() + 1, maxAttempts);
							retryCounter.increment();
						})
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
							log.error("External service failed to process after max retries: {}", maxAttempts);
							int statusCode = HttpStatus.SERVICE_UNAVAILABLE.value();
							if (retrySignal.failure() instanceof ReceiverUnavailableException) {
								statusCode = ((ReceiverUnavailableException) retrySignal.failure()).getHttpStatusCode();
							}
							throw new ExternalServiceUnavailable(retrySignal.failure().getMessage(), statusCode);
						})
				)
				.doOnError(e -> log.error("Final error after all retries: {}", e.getMessage()))
				.doOnSuccess((e) -> log.info("The call to the remote service was successful!"));
	}
}
