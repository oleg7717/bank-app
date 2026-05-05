package ru.goncharenko.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.cashoperation.BalanceDto;
import ru.goncharenko.bankclient.common.model.cashoperation.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.reactive.service.AntifraudSendService;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.service.WebClientService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.CASH;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {
	private final WebClientService webClientService;
	private final NotificationSendService notificationSendService;
	private final AntifraudSendService antifraudSendService;
	private final SecurityUtils securityUtils;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url}")
	private String accountUrl;


	public Mono<BalanceDto> depositOrWithdraw(DepositOrWithdrawDto dto) {
		return securityUtils.getAuthorize(service)
				.flatMap(antifraudSendService.makeFraudCheck(service, dto))
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + CASH,
								service,
								dto,
								BalanceDto.class
						).flatMap(resp ->
								securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
										.sendNotification(
												service,
												String.format("Пополнение / снятие средств со счёта %s успешно выполнено", userName)
										)
										.thenReturn(resp))
						)
				);
	}
}
