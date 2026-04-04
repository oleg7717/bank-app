package ru.goncharenko.bankfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.goncharenko.bankclient.config.RestClientAutoConfiguration;
import ru.goncharenko.bankclient.config.RestClientService;

@SpringBootApplication
@Import({RestClientAutoConfiguration.class, RestClientService.class})
public class BankFrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankFrontApplication.class, args);
	}
}
