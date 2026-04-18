package ru.goncharenko.bankclient.reactive.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/*
* Фильтр для сквозной аутентификации пользователя при передаче запроса в другие микросервисы по цепочке
* */
@Slf4j
@Component
public class JwtAuthFilter implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return ReactiveSecurityContextHolder.getContext()
				.flatMap(context -> {
					String token = extractToken(context);
					if (token != null) {
						log.debug("Adding JWT token to request: {}", request.url());
						ClientRequest authenticatedRequest = ClientRequest.from(request)
								.headers(headers -> headers.setBearerAuth(token))
								.build();
						return next.exchange(authenticatedRequest);
					}
					log.debug("No JWT token found for request: {}", request.url());
					return next.exchange(request);
				})
				.switchIfEmpty(Mono.defer(() -> {
					log.debug("Security context empty for request: {}", request.url());
					return next.exchange(request);
				}));
	}

	private String extractToken(org.springframework.security.core.context.SecurityContext context) {
		if (context == null || context.getAuthentication() == null) {
			return null;
		}

		Object principal = context.getAuthentication().getPrincipal();

		if (principal instanceof DefaultOidcUser oidcUser) {
			return oidcUser.getIdToken().getTokenValue();
		} else if (principal instanceof Jwt jwt) {
			return jwt.getTokenValue();
		}

		return null;
	}
}
