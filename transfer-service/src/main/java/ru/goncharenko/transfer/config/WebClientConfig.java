package ru.goncharenko.transfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	@Value("${application.service.account.url:http://account-service}")
	private String accountUrl;

	@Bean
	public WebClient restClient() {
		return WebClient.create(accountUrl);
	}
}
