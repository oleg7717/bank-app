package ru.goncharenko.bankclient.reactive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.enums.AntifraudCheckStatus;
import ru.goncharenko.bankclient.common.mapper.AntifraudMapper;
import ru.goncharenko.bankclient.common.model.AntifraudDto;
import ru.goncharenko.bankclient.common.response.AntiFraudResponse;

import java.util.function.Function;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
@ConditionalOnClass(WebClient.class)
public class AntifraudSendService {
	private final String antifraudUrl;
	private final WebClientService webClientService;
	private final AntifraudMapper antifraudMapper;

	public AntifraudSendService(@Value("${application.service.antifraud.url:http://antifraud-service}") String antifraudBaseUrl,
	                               final WebClientService webClientService,
	                            AntifraudMapper antifraudMapper) {
		this.antifraudUrl = antifraudBaseUrl + ANTIFRAUD_BASE_URL + FRAUD_CHECK;
		this.webClientService = webClientService;
		this.antifraudMapper = antifraudMapper;
	}

	public <T, R> Function<? super T, ? extends Mono<? extends T>> makeFraudCheck(String service, R dto) {
		return client -> {
			AntifraudDto antifraudDto = antifraudMapper.mapToAntifraudDto(dto);
			return callAntifraudServiceCheck(service, antifraudDto)
					.flatMap(antiFraudResponse -> {
						if (antiFraudResponse.getStatus() == AntifraudCheckStatus.BLOCKED) {
							return Mono.error(new ResponseStatusException(
									HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS,
									antiFraudResponse.getMessage()
							));
						}
						return Mono.just(client);
					});
		};
	}

	private Mono<AntiFraudResponse> callAntifraudServiceCheck(String service, AntifraudDto message) {
		return webClientService.postForObject(antifraudUrl, service, message, AntiFraudResponse.class);
	}
}
