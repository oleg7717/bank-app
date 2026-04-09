package ru.goncharenko.cash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.goncharenko.bankclient.config.WebClientAutoConfig;
import ru.goncharenko.bankclient.service.WebClientService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

@SpringBootApplication
@EnableWebFlux
@Import({WebClientAutoConfig.class, WebClientService.class, SecurityUtils.class})
public class CashServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashServiceApplication.class, args);
	}
}
