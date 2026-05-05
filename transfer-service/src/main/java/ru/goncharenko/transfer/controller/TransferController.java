package ru.goncharenko.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;
import ru.goncharenko.transfer.service.TransferService;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.TRANSFER_BASE_URL;

@RestController
@RequestMapping(TRANSFER_BASE_URL)
@RequiredArgsConstructor
public class TransferController {
	private final TransferService service;

	@PostMapping
	public Mono<SuccessResponse> transferBetweenClientAccounts(@RequestBody TransferBetweenAccountsDto dto) {
		return service.transferBetweenClients(dto);
	}

	@PostMapping
	public Mono<SuccessResponse> transferCash(@RequestBody TransferBetweenOwnAccountsDto dto) {
		return service.transferBetweenOwnAccounts(dto);
	}
}
