package ru.goncharenko.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.goncharenko.bankclient.config.JwtAuthFilter;
import ru.goncharenko.bankclient.config.WebClientAutoConfig;
import ru.goncharenko.bankclient.service.NotificationSendService;
import ru.goncharenko.bankclient.service.WebClientService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

@SpringBootApplication
@Import({WebClientAutoConfig.class, WebClientService.class, NotificationSendService.class, SecurityUtils.class, JwtAuthFilter.class})
@EnableWebFlux
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}
