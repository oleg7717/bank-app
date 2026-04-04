package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.service.TransferService;
import ru.goncharenko.bankclient.model.TransferCashDto;
import ru.goncharenko.bankclient.response.SuccessResponse;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@RestController
@RequestMapping(ACCOUNT_BASE_URL)
@RequiredArgsConstructor
public class TransferController {
	private final TransferService service;

	@PostMapping(TRANSFER)
	public Mono<ResponseEntity<SuccessResponse>>transferCash(@RequestBody Mono<TransferCashDto> transferCashDto) {
		return service.transferCash(transferCashDto);
	}
}
