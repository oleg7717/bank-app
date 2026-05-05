package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.service.TransferService;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@RestController
@RequestMapping(ACCOUNT_BASE_URL)
@RequiredArgsConstructor
public class TransferController {
	private final TransferService service;

	@PostMapping(TRANSFER)
	public Mono<ResponseEntity<SuccessResponse>>transferBetweenClientAccounts(@RequestBody Mono<TransferBetweenAccountsDto> transferCashDto) {
		return service.transferBetweenClientAccounts(transferCashDto);
	}

	@PostMapping(OWN_TRANSFER)
	public Mono<ResponseEntity<SuccessResponse>>transferBetweenOwnAccounts(@RequestBody Mono<TransferBetweenOwnAccountsDto> transferCashDto) {
		return service.transferBetweenOwnAccounts(transferCashDto);
	}
}
