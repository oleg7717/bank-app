package ru.goncharenko.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goncharenko.bankclient.common.model.CurrencyRateDto;
import ru.goncharenko.bankclient.common.request.ConversionRequestDto;
import ru.goncharenko.bankclient.common.response.ConversionResponseDto;
import ru.goncharenko.exchange.service.ExchangeService;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@RestController
@RequestMapping(EXCHANGE_BASE_URL)
@RequiredArgsConstructor
public class ExchangeController {
	private final ExchangeService service;

	@PostMapping(CONVERT)
	public ConversionResponseDto convert(@RequestBody @Valid ConversionRequestDto conversionRequest) {
		return service.convert(conversionRequest);
	}

	@PostMapping(RATES)
	public void updateRates(@RequestBody @Valid CurrencyRateDto conversionRequest) {
		service.createOrUpdateRates(conversionRequest);
	}
}
