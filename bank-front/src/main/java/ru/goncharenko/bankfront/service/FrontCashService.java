package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.common.model.cashoperation.DepositOrWithdrawDto;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.CASH_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.CASH_GATEWAY;

@Slf4j
@Service
public class FrontCashService {
	private final String cashBaseUrl;
	private final RestClientService restClient;

	public FrontCashService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                        final RestClientService restClient) {
		this.cashBaseUrl = gatewayUrl + CASH_GATEWAY + CASH_BASE_URL;
		this.restClient = restClient;
	}

	@PreAuthorize("hasRole('cash_deposit_or_withdraw')")
	public void depositOrWithdraw(DepositOrWithdrawDto depositOrWithdrawDto, Class<?> responseClass) {
		try {
			restClient.postForObject(cashBaseUrl, depositOrWithdrawDto, responseClass);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
