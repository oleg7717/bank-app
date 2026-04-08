package ru.goncharenko.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.service.WebClientService;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {
	private final WebClientService webClientService;

	@Value("${application.service.account.url}")
	private String accountUrl;


	public Mono<BalanceDto> depositOrWithdraw(DepositOrWithdrawDto dto) {
		return webClientService.postForObject(accountUrl + ACCOUNT_BASE_URL + CASH, dto, BalanceDto.class);
	}
}
