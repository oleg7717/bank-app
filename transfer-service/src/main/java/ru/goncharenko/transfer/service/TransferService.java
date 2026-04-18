package ru.goncharenko.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.TransferCashDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;
import ru.goncharenko.bankclient.reactive.service.AntifraudSendService;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.service.WebClientService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.TRANSFER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
	private final WebClientService webClientService;
	private final NotificationSendService notificationSendService;
	private final AntifraudSendService antifraudSendService;
	private final SecurityUtils securityUtils;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url:http//account-service}")
	private String accountUrl;

	public Mono<SuccessResponse> transferCash(TransferCashDto dto) {
		return securityUtils.getAuthorize(service)
				.flatMap(antifraudSendService.makeFraudCheck(service, dto))
/*				.flatMap(client -> {
					AntifraudDto antifraudDto = antifraudMapper.mapToAntifraudDto(dto);
					return antifraudSendService.makeFraudCheck(service, antifraudDto)
							.flatMap(antiFraudResponse -> {
								if (antiFraudResponse.getStatus() == AntifraudCheckStatus.BLOCKED) {
									return Mono.error(new ResponseStatusException(
											HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS,
											antiFraudResponse.getMessage()
									));
								}
								return Mono.just(client);
							});
				})*/
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + TRANSFER,
								service,
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
