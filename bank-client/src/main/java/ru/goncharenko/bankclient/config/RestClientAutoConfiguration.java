package ru.goncharenko.bankclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Configuration
public class RestClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder()
				.defaultStatusHandler(HttpStatusCode::is4xxClientError,
						(req, resp) -> {
							throw new RestClientException("HTTP " + resp.getStatusCode());
						});
	}

	@Bean
	@ConditionalOnMissingBean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
