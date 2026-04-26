package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferCashDto;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
public class FrontTransferService {
	private final String transferBaseUrl;
	private final RestClientService restClient;

	public FrontTransferService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                            final RestClientService restClient) {
		this.transferBaseUrl = gatewayUrl + TRANSFER_GATEWAY + TRANSFER_BASE_URL;
		this.restClient = restClient;
	}

	@PreAuthorize("hasRole('transfer_cash')")
	public void transferCash(TransferCashDto transferCashDto, Class<?> responseClass) {
		try {
			restClient.postForObject(transferBaseUrl, transferCashDto, responseClass);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
