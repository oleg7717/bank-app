package ru.goncharenko.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.TransferCashDto;
import ru.goncharenko.bankclient.response.SuccessResponse;
import ru.goncharenko.bankclient.service.NotificationSendService;
import ru.goncharenko.bankclient.service.WebClientService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

import static ru.goncharenko.bankclient.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.endpoint.Endpoints.TRANSFER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
	private final WebClientService webClientService;
	private final NotificationSendService notificationSendService;
	private final SecurityUtils securityUtils;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url}")
	private String accountUrl;

	public Mono<SuccessResponse> transferCash(TransferCashDto dto) {
		return securityUtils.getAuthorize("transfer-service")
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + TRANSFER,
								"transfer-service",
								dto,
								SuccessResponse.class
						)
				).flatMap(resp ->
						securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
								.sendNotification(service,
										String.format("Перевод средств со счёта %s успешно выполнен", userName)
								)
								.thenReturn(resp))

				);
	}
}
