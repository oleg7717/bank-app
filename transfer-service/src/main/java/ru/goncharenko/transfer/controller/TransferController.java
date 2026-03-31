package ru.goncharenko.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.model.TransferCashDto;
import ru.goncharenko.bankclient.response.SuccessResponse;
import ru.goncharenko.transfer.service.TransferService;

import static ru.goncharenko.bankclient.endpoint.Endpoints.TRANSFER_BASE_URL;

@RestController(TRANSFER_BASE_URL)
@RequiredArgsConstructor
public class TransferController {
	private final TransferService service;

	@PostMapping
	public Mono<SuccessResponse> transferCash(@RequestBody TransferCashDto dto) {
		return service.transferCash(dto);
	}
}
