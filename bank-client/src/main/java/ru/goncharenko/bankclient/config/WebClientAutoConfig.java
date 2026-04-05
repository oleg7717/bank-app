package ru.goncharenko.bankclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientAutoConfig {
	@Bean
	@ConditionalOnMissingBean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder()
				.defaultStatusHandler(HttpStatusCode::is4xxClientError,
						(resp) ->
								Mono.error(new RuntimeException("HTTP " + resp.bodyToMono(String.class)))
				);
	}

	@Bean
	@ConditionalOnMissingBean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}
}
