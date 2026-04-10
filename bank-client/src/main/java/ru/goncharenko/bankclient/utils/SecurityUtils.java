package ru.goncharenko.bankclient.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {
	private final ReactiveOAuth2AuthorizedClientManager manager;

	public Mono<String> getCurrentUsername() {
		return ReactiveSecurityContextHolder.getContext()
				.map(context -> {
					Object principal = Objects.requireNonNull(context.getAuthentication()).getPrincipal();
					if (principal instanceof Jwt jwt) {
						return (String) jwt.getClaims().get("preferred_username");
					}
					return "unknown";
				})
				.doOnNext(username -> log.info("Current user: {}", username))
				.switchIfEmpty(Mono.just("anonymous"))
				.onErrorResume(e -> {
					log.error("Error getting security context", e);
					return Mono.just("anonymous");
				});
	}

	public Mono<Authentication> getCurrentAuthentication() {
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.doOnNext(auth -> log.info("Authentication name: {}", auth.getName()))
				.switchIfEmpty(Mono.error(new RuntimeException("No authentication found")));
	}

	public Mono<OAuth2AuthorizedClient> getAuthorize(String clientId) {
		return manager.authorize(OAuth2AuthorizeRequest
				.withClientRegistrationId(clientId)
				.principal("system") // У client_credentials нет имени пользователя, поэтому используется system
				.build()
		);
	}
}

