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

	@Value("${application.currencies}")
	private List<String> currencies;

	public void generateAndSendRates() {
		log.info("Generating currency rates...");

		for (String from : currencies) {
			for (String to : currencies) {
				if (!from.equals(to)) {
					Double rate = generateRate(from, to);
					exchangeClient.sendRate(from, to, rate);
				}
			}
		}
	}

	private Double generateRate(String from, String to) {
		if (from.equals("RUB")) {
			return switch (to) {
				case "USD" -> 0.01 + random.nextDouble() * 0.005; // 0.01 - 0.015
				case "CNY" -> 0.075 + random.nextDouble() * 0.01; // 0.075 - 0.085
				default -> 1.0;
			};
		} else if (to.equals("RUB")) {
			return switch (from) {
				case "USD" -> 90 + random.nextDouble() * 10; // 90 - 100
				case "CNY" -> 12 + random.nextDouble() * 2; // 12 - 14
				default -> 1.0;
			};
		} else {
			// Генерация кросс-курсов
			return 0.5 + random.nextDouble() * 1.5;
		}
	}
}