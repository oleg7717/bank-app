package ru.goncharenko.exchange.cient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.goncharenko.bankclient.common.model.CurrencyRateDto;
import ru.goncharenko.bankclient.web.service.RestClientService;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.EXCHANGE_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.RATES;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeClient {
	private final RestClientService restClientService;

	@Value("${application.service.exchange.url:http://exchange-service}")
	private String exchangeServiceUrl;

	public void sendRate(String from, String to, Double rate) {
		String url = exchangeServiceUrl + EXCHANGE_BASE_URL + RATES;
		CurrencyRateDto rateDto = CurrencyRateDto.builder()
				.fromCurrency(from)
				.toCurrency(to)
				.rate(rate)
				.build();

		try {
			restClientService.postForObject(url, rateDto, Void.class);
			log.info("Sent rate to exchange service: {} -> {} = {}", from, to, rate);
		} catch (Exception e) {
			log.error("Failed to send rate to exchange service: {}", e.getMessage());
		}
	}
}
