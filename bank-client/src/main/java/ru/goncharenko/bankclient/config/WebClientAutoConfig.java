package ru.goncharenko.bankclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class WebClientAutoConfig {
//	private final JwtAuthFilter jwtAuthFilter;

	@Bean(name = "serviceWebClient")
	@Primary
	@ConditionalOnMissingBean(name = "serviceWebClient")
	public WebClient webClient() {
		return WebClient.builder()
//				.filter(jwtAuthFilter) //Реализация для передачи пользовательского токена для сквозной аутентификации
				.defaultStatusHandler(HttpStatusCode::is4xxClientError,
						(resp) ->
								Mono.error(new RuntimeException("HTTP " + resp.bodyToMono(String.class)))
				).build();
	}
}
