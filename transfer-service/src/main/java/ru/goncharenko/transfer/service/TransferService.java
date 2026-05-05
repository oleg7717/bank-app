package ru.goncharenko.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsDto;
import ru.goncharenko.bankclient.common.request.ConversionRequestDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;
import ru.goncharenko.bankclient.reactive.service.AntifraudSendService;
import ru.goncharenko.bankclient.reactive.service.ExchangeSendService;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.service.WebClientService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
	private final WebClientService webClientService;
	private final ExchangeSendService exchangeSendService;
	private final NotificationSendService notificationSendService;
	private final AntifraudSendService antifraudSendService;
	private final SecurityUtils securityUtils;

	@Value("${spring.application.name}")
	private String service;

	@Value("${application.service.account.url:http//account-service}")
	private String accountUrl;

	public Mono<SuccessResponse> transferBetweenClients(TransferBetweenAccountsDto dto) {
		return securityUtils.getAuthorize(service)
				.flatMap(antifraudSendService.makeFraudCheck(service, dto))
				.flatMap(client -> {
					var conversionRequestDto = ConversionRequestDto.builder()
							.fromCurrency(dto.getFromCurrency())
							.toCurrency(dto.getToCurrency())
							.amount(dto.getAmount())
							.build();
					return exchangeSendService.convertMoney(service, conversionRequestDto);
				})
				.flatMap(conversionResponse -> {
							dto.setAmount(conversionResponse.getConvertedAmount());
							return webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + TRANSFER,
									service,
									dto,
									SuccessResponse.class
							);
						}
				).flatMap(resp ->
						securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
								.sendNotification(service,
										String.format("Перевод средств со счёта %s успешно выполнен", userName)
								)
								.thenReturn(resp))

				);
	}

	public Mono<SuccessResponse> transferBetweenOwnAccounts(TransferBetweenOwnAccountsDto dto) {
		return securityUtils.getAuthorize(service)
				.flatMap(antifraudSendService.makeFraudCheck(service, dto))
				.flatMap(client -> {
					var conversionRequestDto = ConversionRequestDto.builder()
							.fromCurrency(dto.getFromCurrency())
							.toCurrency(dto.getToCurrency())
							.amount(dto.getAmount())
							.build();
					return exchangeSendService.convertMoney(service, conversionRequestDto);
				})
				.flatMap(conversionResponse -> {
							dto.setAmount(conversionResponse.getConvertedAmount());
							return webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + OWN_TRANSFER,
									service,
									dto,
									SuccessResponse.class
							);
						}
				).flatMap(resp ->
						securityUtils.getCurrentUsername().flatMap(userName -> notificationSendService
								.sendNotification(service,
										String.format("Перевод средств со счёта %s успешно выполнен", userName)
								)
								.thenReturn(resp))

				);
	}
}
