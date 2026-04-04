package ru.goncharenko.bankfront.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.config.RestClientService;
import ru.goncharenko.bankclient.enums.CashAction;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.CASH_BASE_URL;

@Service
public class FrontCashService {
	private final String login = "o.goncharenko";
	private final String cashBaseUrl;
	private final RestClientService restClient;

	public FrontCashService(@Value("${application.service.cash.url:http://localhost:8082}") String cashUrl, final RestClientService restClient) {
		this.cashBaseUrl = cashUrl + CASH_BASE_URL;
		this.restClient = restClient;
	}

	public void depositOrWithdraw(int value, CashAction action) {
		try {
			DepositOrWithdrawDto depositOrWithdraw = new DepositOrWithdrawDto(login, (double) value, action);
			restClient.postForObject(cashBaseUrl, depositOrWithdraw, BalanceDto.class);
		} catch (RestClientException e) {
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
		}
	}
}
