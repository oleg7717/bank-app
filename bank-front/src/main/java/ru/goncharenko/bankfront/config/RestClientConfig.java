package ru.goncharenko.bankfront.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
	@Value("${application.service.account.url:http://localhost:8080}")
	private String accountUrl;

	@Bean
	public RestClient restClient() {
		return RestClient.create(accountUrl);
	}
}
