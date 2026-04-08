package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.service.RestClientService;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.TransferCashDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.TRANSFER_BASE_URL;

@Slf4j
@Service
public class FrontTransferService {
	private final String login = "o.goncharenko";
	private final String transferBaseUrl;
	private final RestClientService restClient;

	public FrontTransferService(@Value("${application.service.trsnsfer.url:http://localhost:8083}") String transferUrl,
	                            final RestClientService restClient) {
		this.transferBaseUrl = transferUrl + TRANSFER_BASE_URL;
		this.restClient = restClient;
	}

	@PreAuthorize("hasRole('transfer_cash')")
	public void transferCash(int value, String accountTo) {
		try {
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
