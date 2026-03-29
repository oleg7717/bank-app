package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;
import ru.goncharenko.account.service.CashService;
import ru.goncharenko.bankclient.model.BalanceDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.CASH_BASE_URL;

@RestController(CASH_BASE_URL)
@RequiredArgsConstructor
public class CashController {
	private final CashService service;

	@PostMapping
	public Mono<BalanceDto> depositOrWithdrawMoney(@RequestBody DepositOrWithdrawDto dto) {
		return service.depositOrWithdraw(dto);
	}
}
