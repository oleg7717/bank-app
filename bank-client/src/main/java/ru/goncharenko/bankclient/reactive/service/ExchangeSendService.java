package ru.goncharenko.bankclient.reactive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.request.ConversionRequestDto;
import ru.goncharenko.bankclient.common.response.ConversionResponseDto;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
@ConditionalOnClass(WebClient.class)
public class ExchangeSendService {
	private final String exchangeUrl;
	private final WebClientService webClientService;

	public ExchangeSendService(@Value("${application.service.exchange.url:http://exchange-service}") String antifraudBaseUrl,
	                           final WebClientService webClientService) {
		this.exchangeUrl = antifraudBaseUrl + EXCHANGE_BASE_URL + CONVERT;
		this.webClientService = webClientService;
	}

	public Mono<ConversionResponseDto> convertMoney(String service, ConversionRequestDto dto) {
		return webClientService.postForObject(exchangeUrl, service, dto, ConversionResponseDto.class);
	}
}
