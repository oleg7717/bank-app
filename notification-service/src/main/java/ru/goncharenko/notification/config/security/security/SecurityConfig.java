package ru.goncharenko.notification.config.security.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String JwtIssuerUri;

	@Bean
	SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) {
		return security
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
						.anyExchange().hasRole("notification")
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtAuthenticationConverter(jwtAuthenticationConverter())
						)
				)
				.build();
	}

	@Bean
	ReactiveJwtDecoder reactiveJwtDecoder() {
		return ReactiveJwtDecoders.fromIssuerLocation(JwtIssuerUri);
	}

	@Bean
	ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
		ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
			if (resourceAccess == null) {
				return Flux.empty();
			}

			Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account-service");
			Map<String, Object> cash = (Map<String, Object>) resourceAccess.get("cash-service");
			Map<String, Object> transfer = (Map<String, Object>) resourceAccess.get("transfer-service");

			List<String> roles;
			if (account != null) {
				roles = (List<String>) account.get("roles");
			} else if (cash != null) {
				roles = (List<String>) cash.get("roles");
			} else if (transfer != null) {
				roles = (List<String>) transfer.get("roles");
			} else {
				return Flux.empty();
			}

			return Flux.fromIterable(roles)
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role));
		});
		return converter;
	}
}
