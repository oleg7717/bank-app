package ru.goncharenko.transfer.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.TransferCashDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;
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
	private final SecurityUtils securityUtils;
	private final MeterRegistry meterRegistry;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url:http//account-service}")
	private String accountUrl;

	public Mono<SuccessResponse> transferCash(TransferCashDto dto) {
		return securityUtils.getAuthorize("transfer-service")
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + TRANSFER,
								service,
								dto,
								SuccessResponse.class
						).doOnError(error -> {
							log.error("Error while transfer money");
							Counter.builder("cash_transfer_error")
									.tag("login", dto.getFromAccount())
									.register(meterRegistry)
									.increment();
						})
				).flatMap(resp ->
						securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
								.sendNotification(service,
										String.format("Перевод средств со счёта %s успешно выполнен", userName)
								)
								.doOnError(error -> Counter.builder("notification_send_error")
										.tag("service", service)
										.register(meterRegistry)
										.increment())
								.thenReturn(resp))

				);
	}
}
