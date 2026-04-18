package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.service.CashService;
import ru.goncharenko.bankclient.common.model.BalanceDto;
import ru.goncharenko.bankclient.common.model.DepositOrWithdrawDto;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.CASH;

@RestController
@RequestMapping(ACCOUNT_BASE_URL)
@RequiredArgsConstructor
public class CashController {
	private final CashService cashService;

	@PostMapping(CASH)
	public Mono<ResponseEntity<BalanceDto>> depositOrWithdrawCash(@RequestBody Mono<DepositOrWithdrawDto> depositOrWithdrawDto) {
		return cashService.depositOrWithdraw(depositOrWithdrawDto);
	}
}
