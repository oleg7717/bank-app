package ru.goncharenko.notification.config.security.security.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {
	public Mono<String> getCurrentUsername() {
		return ReactiveSecurityContextHolder.getContext()
				.flatMap(context -> {
							Object auth = requireNonNull(context.getAuthentication()).getCredentials();
							String userName = "anonymous";
							if (auth instanceof Jwt credentials) {
								userName = (String) credentials.getClaims().get("preferred_username");
							}
							return Mono.just(userName);
						}
						)
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
}
