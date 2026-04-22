package ru.goncharenko.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.goncharenko.bankclient.common.request.ConversionRequestDto;
import ru.goncharenko.bankclient.common.response.ConversionResponseDto;
import ru.goncharenko.exchange.mapper.CurrencyRateMapper;
import ru.goncharenko.exchange.model.CurrencyRate;
import ru.goncharenko.bankclient.common.model.CurrencyRateDto;
import ru.goncharenko.exchange.repository.CurrencyRateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {
	private final CurrencyRateRepository repository;
	private final CurrencyRateMapper mapper;

	public ConversionResponseDto convert(ConversionRequestDto request) {
		String from = request.getFromCurrency();
		String to = request.getToCurrency();
		Double amount = request.getAmount();

		log.info("Converting {} {} to {}", amount, from, to);

		Double rate = getConversionRate(from, to);
		Double convertedAmount = amount * rate;

		return ConversionResponseDto.builder()
				.fromCurrency(from)
				.toCurrency(to)
				.originalAmount(amount)
				.convertedAmount(convertedAmount)
				.build();
	}

	public void createOrUpdateRates(CurrencyRateDto rateDto) {
		repository.findByFromCurrencyAndToCurrency(rateDto.getFromCurrency(), rateDto.getToCurrency())
				.ifPresentOrElse(currencyRate -> {
					currencyRate.setRate(currencyRate.getRate());
					currencyRate.setUpdatedAt(currencyRate.getUpdatedAt());
					repository.save(currencyRate);
				}, () -> {
					var currencyRate = mapper.dtoToEntity(rateDto);
					repository.save(currencyRate);
				});
	}

	private Double getConversionRate(String from, String to) {
		if (from.equals(to)) {
			return 1.0;
		}

		// Базовая валюта RUB
		if (from.equals("RUB")) {
			return getRateValue("RUB", to);
		} else if (to.equals("RUB")) {
			return getRateValue(from, "RUB");
		} else {
			// Конвертация через RUB: from -> RUB -> to
			Double toRub = getRateValue(from, "RUB");
			Double fromRub = getRateValue("RUB", to);
			return (1.0 / toRub) * fromRub;
		}
	}

	private Double getRateValue(String from, String to) {
		return repository.findByFromCurrencyAndToCurrency(from, to)
				.map(CurrencyRate::getRate)
				.orElseThrow(() -> new RuntimeException("Курс валюты не найден: " + from + " -> " + to));
	}
}
