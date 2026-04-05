package ru.goncharenko.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.goncharenko.bankclient.config.WebClientAutoConfig;
import ru.goncharenko.bankclient.service.NotificationSendService;

@SpringBootApplication
@Import({WebClientAutoConfig.class, NotificationSendService.class})
@EnableWebFlux
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}
