package ru.goncharenko.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.TransferCashDto;
import ru.goncharenko.bankclient.response.SuccessResponse;
import ru.goncharenko.bankclient.service.WebClientService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
	private final WebClientService webClientService;
	private final SecurityUtils securityUtils;

	@Value("${application.service.account.url}")
	private String accountUrl;

	public Mono<SuccessResponse> transferCash(TransferCashDto dto) {
		return securityUtils.getAuthorize("transfer-service")
				.doOnSubscribe(sub -> log.info("Starting account service call"))
				.doOnSuccess(token -> log.info("Token obtained successfully"))
				.doOnError(error -> log.error("Failed to obtain token", error))
				.flatMap(client ->
						webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + TRANSFER,
								client.getAccessToken().getTokenValue(),
								dto,
								SuccessResponse.class
						)
				);
	}
}
