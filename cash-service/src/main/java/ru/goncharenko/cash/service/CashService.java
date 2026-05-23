package ru.goncharenko.cash.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.BalanceDto;
import ru.goncharenko.bankclient.common.model.DepositOrWithdrawDto;
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
	private final SecurityUtils securityUtils;
	private final MeterRegistry meterRegistry;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url}")
	private String accountUrl;


	public Mono<BalanceDto> depositOrWithdraw(DepositOrWithdrawDto dto) {
		return securityUtils.getAuthorize("cash-service")
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + CASH,
								service,
								dto,
								BalanceDto.class
						).doOnError(error -> {
							log.error("Error while deposit or withdraw money");
							Counter.builder("cash_deposit_error")
									.tag("login", dto.getLogin())
									.register(meterRegistry)
									.increment();
						}).flatMap(resp ->
								securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
										.sendNotification(
												service,
												String.format("Пополнение / снятие средств со счёта %s успешно выполнено", userName)
										).doOnError(error -> Counter.builder("notification_send_error")
												.tag("service", service)
												.register(meterRegistry)
												.increment())
										.thenReturn(resp))
						)
				);
	}
}
