package ru.goncharenko.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {
	private final WebClient webClient;

	public Mono<BalanceDto> depositOrWithdraw(DepositOrWithdrawDto dto) {
		return webClient.post()
				.uri(ACCOUNT_BASE_URL + CASH)
				.bodyValue(dto)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (response) ->
						response.bodyToMono(String.class).flatMap(error -> {
							log.error("Custom 4xx handler: {}", error);
							return Mono.error(new ResponseStatusException(
									HttpStatus.BAD_REQUEST,
									error
							));
						}))
				.bodyToMono(BalanceDto.class);
	}
}
