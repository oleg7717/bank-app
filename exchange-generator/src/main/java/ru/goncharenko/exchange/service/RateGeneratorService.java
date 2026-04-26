package ru.goncharenko.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.goncharenko.exchange.cient.ExchangeClient;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateGeneratorService {
	private final ExchangeClient exchangeClient;
	private final Random random = new Random();
	private final String baseCurrency = "RUB";

	@Value("${application.currencies}")
	private List<String> currencies;

	public void generateAndSendRates() {
		log.info("Generating currency rates...");

		for (String to : currencies) {
			Double rate = generateRate(to);
			exchangeClient.sendRate(baseCurrency, to, rate);
			exchangeClient.sendRate(to, baseCurrency, 1 / rate);
		}
	}

	private Double generateRate(String to) {
		return switch (to) {
			case "USD" -> 0.01 + random.nextDouble() * 0.005; // 0.01 - 0.015
			case "CNY" -> 0.075 + random.nextDouble() * 0.01; // 0.075 - 0.085
			default -> 1.0;
		};
	}
}