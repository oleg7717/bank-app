package ru.goncharenko.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.model.BalanceDto;
import ru.goncharenko.cash.service.CashService;

import static ru.goncharenko.bankclient.endpoint.Endpoints.CASH_BASE_URL;

@RestController
@RequestMapping(CASH_BASE_URL)
@RequiredArgsConstructor
public class CashController {
	private final CashService service;

	@PostMapping
	public Mono<BalanceDto> depositOrWithdrawCash(@RequestBody DepositOrWithdrawDto dto) {
		return service.depositOrWithdraw(dto);
	}
}
