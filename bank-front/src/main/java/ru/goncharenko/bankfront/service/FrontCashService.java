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
import ru.goncharenko.bankfront.config.security.utils.SecurityUtils;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Slf4j
@Service
public class FrontCashService {
	private final String cashBaseUrl;
	private final RestClientService restClient;
	private final SecurityUtils securityUtils;

	public FrontCashService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                        final RestClientService restClient,
	                        SecurityUtils securityUtils) {
		this.cashBaseUrl = gatewayUrl + CASH_GATEWAY + CASH_BASE_URL;
		this.restClient = restClient;
		this.securityUtils = securityUtils;
	}

	@PreAuthorize("hasRole('cash_deposit_or_withdraw')")
	public void depositOrWithdraw(int value, CashAction action) {
		try {
			String login = securityUtils.getCurrentUsername();
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
