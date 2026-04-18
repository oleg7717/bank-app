package ru.goncharenko.cash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class CashServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashServiceApplication.class, args);
	}
}
