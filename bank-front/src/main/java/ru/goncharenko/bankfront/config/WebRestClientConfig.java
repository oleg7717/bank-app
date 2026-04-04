package ru.goncharenko.bankfront.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebRestClientConfig {
	@Value("${application.service.account.url:http://localhost:8081}")
	private String accountUrl;

	public RestClient restClient() {
		return RestClient.create(accountUrl);
	}
}
