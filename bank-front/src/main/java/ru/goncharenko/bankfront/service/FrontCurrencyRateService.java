package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.common.model.CurrencyOperationRateDto;

import java.util.List;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
public class FrontCurrencyRateService {
	private final RestClientService restClient;
	private final String exchangeBaseUrl;

	public FrontCurrencyRateService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                                final RestClientService restClient) {
		this.exchangeBaseUrl = gatewayUrl + EXCHANGE_GATEWAY + RATES;
		this.restClient = restClient;
	}

	public List<CurrencyOperationRateDto> getCurrencyRates() {
		try {
			ParameterizedTypeReference<List<CurrencyOperationRateDto>> typeRef = new ParameterizedTypeReference<>() {};
			return restClient.getForObject(exchangeBaseUrl, typeRef);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
