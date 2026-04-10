package ru.goncharenko.bankclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class WebClientAutoConfig {
//	private final JwtAuthFilter jwtAuthFilter;

	@Bean(name = "serviceWebClient")
	@Primary
	@ConditionalOnMissingBean(name = "serviceWebClient")
	public WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations,
	                           ServerOAuth2AuthorizedClientRepository authorizedClients) {
		// 1. Создаем фильтр OAuth2
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
				new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);

		// 2. Указываем, какой Registration ID использовать по умолчанию (тот, что в yml)
		// Фильтр сам получит и подставит токен для этого клиента
		oauthFilter.setDefaultClientRegistrationId(null);

		// 3. (Опционально) Если у вас нет пользовательского контекста, можно явно указать,
		// что токен нужен от клиента, а не от пользователя.
		oauthFilter.setDefaultOAuth2AuthorizedClient(true);

		return WebClient.builder()
//				.filter(jwtAuthFilter) //Реализация для передачи пользовательского токена для сквозной аутентификации
				.filter(oauthFilter)
				.defaultStatusHandler(HttpStatusCode::is4xxClientError,
						(resp) ->
								Mono.error(new RuntimeException("HTTP " + resp.bodyToMono(String.class)))
				).build();
	}
}
