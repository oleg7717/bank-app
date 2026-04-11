package ru.goncharenko.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.service.NotificationSendService;
import ru.goncharenko.bankclient.service.WebClientService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

import static ru.goncharenko.bankclient.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.endpoint.Endpoints.CASH;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {
	private final WebClientService webClientService;
	private final NotificationSendService notificationSendService;
	private final SecurityUtils securityUtils;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url}")
	private String accountUrl;


	public Mono<BalanceDto> depositOrWithdraw(DepositOrWithdrawDto dto) {
		return securityUtils.getAuthorize("cash-service")
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + CASH,
								"cash-service",
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
