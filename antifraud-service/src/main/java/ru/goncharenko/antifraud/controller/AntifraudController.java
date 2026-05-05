package ru.goncharenko.antifraud.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goncharenko.antifraud.service.AntifraudService;
import ru.goncharenko.bankclient.common.model.AntifraudDto;
import ru.goncharenko.bankclient.common.response.AntiFraudResponse;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ANTIFRAUD_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.FRAUD_CHECK;

@RestController
@RequestMapping(ANTIFRAUD_BASE_URL)
@RequiredArgsConstructor
public class AntifraudController {
	private final AntifraudService service;

	@PostMapping(FRAUD_CHECK)
	public AntiFraudResponse checkOperation(@RequestBody @Valid AntifraudDto antifraudRequest) {
		return service.checkOperation(antifraudRequest);
	}
}
