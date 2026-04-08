package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.service.RestClientService;
import ru.goncharenko.bankclient.enums.CashAction;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.CASH_BASE_URL;

@Slf4j
@Service
public class FrontCashService {
	private final String login = "o.goncharenko";
	private final String cashBaseUrl;
	private final RestClientService restClient;

	public FrontCashService(@Value("${application.service.cash.url:http://localhost:8082}") String cashUrl,
	                        final RestClientService restClient) {
		this.cashBaseUrl = cashUrl + CASH_BASE_URL;
		this.restClient = restClient;
	}

	@PreAuthorize("hasRole('cash_deposit_or_withdraw')")
	public void depositOrWithdraw(int value, CashAction action) {
		try {
			DepositOrWithdrawDto depositOrWithdraw = DepositOrWithdrawDto.builder()
					.login(login)
					.balance((double) value)
					.action(action)
					.build();
			restClient.postForObject(cashBaseUrl, depositOrWithdraw, BalanceDto.class);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
