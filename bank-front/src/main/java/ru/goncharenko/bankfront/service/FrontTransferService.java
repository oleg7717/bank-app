package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.common.model.BalanceDto;
import ru.goncharenko.bankclient.common.model.TransferCashDto;
import ru.goncharenko.bankfront.config.security.utils.SecurityUtils;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.TRANSFER_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.TRANSFER_GATEWAY;

@Slf4j
@Service
public class FrontTransferService {
	private final String transferBaseUrl;
	private final RestClientService restClient;
	private final SecurityUtils securityUtils;

	public FrontTransferService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                            final RestClientService restClient,
	                            SecurityUtils securityUtils) {
		this.transferBaseUrl = gatewayUrl + TRANSFER_GATEWAY + TRANSFER_BASE_URL;
		this.restClient = restClient;
		this.securityUtils = securityUtils;
	}

	@PreAuthorize("hasRole('transfer_cash')")
	public void transferCash(int value, String accountTo) {
		try {
			String login = securityUtils.getCurrentUsername();
			TransferCashDto transferCash = TransferCashDto.builder()
					.fromAccount(login)
					.toAccount(accountTo)
					.amount((double) value)
					.build();
			restClient.postForObject(transferBaseUrl, transferCash, BalanceDto.class);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
