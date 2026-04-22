package ru.goncharenko.exchange.sheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.goncharenko.exchange.service.RateGeneratorService;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RateScheduler {
	private final RateGeneratorService rateGeneratorService;

	@Scheduled(fixedDelayString = "${application.rate-generator.milliseconds:1000}")
	public void generateRates() {
		rateGeneratorService.generateAndSendRates();
	}
}
