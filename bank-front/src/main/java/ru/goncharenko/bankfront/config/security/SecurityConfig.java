package ru.goncharenko.bankfront.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
	private String issuerUri;

	@Value("${spring.security.oauth2.client.registration.bank-front-service.client-id}")
	private String clientId;

	@Value("${application.service.postLogoutRedirectUri}")
	private String postLogoutRedirectUri;


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/oauth2/**", "/login/**", "/logout/**", "/actuator/**").permitAll()
						.requestMatchers("/account/**").authenticated()
						.anyRequest().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						.defaultSuccessUrl("/account", true)
						.failureUrl("/login?error=true")
						.userInfoEndpoint(userInfo -> userInfo
								.oidcUserService(oidcUserService())  // Кастомный сервис для извлечения ролей
						)
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessHandler(keycloakLogoutSuccessHandler())
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
				)
				.anonymous(anonymous -> anonymous
						.principal("anonymous")
						.authorities("ROLE_GUEST")
				)
				.csrf(AbstractHttpConfigurer::disable)
				.build();
	}

	@Bean
	public GrantedAuthoritiesMapper grantedAuthoritiesMapper(OAuth2AuthorizedClientService authorizedClientService) {
		return (authorities) -> {
			// Создаем Set для хранения всех ролей
			Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);

			// Получаем текущую аутентификацию
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
				try {
					// Загружаем авторизованного клиента по registrationId и имени пользователя
					OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
							oauth2Auth.getAuthorizedClientRegistrationId(),
							oauth2Auth.getName()
					);

					if (authorizedClient != null) {
						// Получаем Access Token
						String accessToken = authorizedClient.getAccessToken().getTokenValue();

						// Извлекаем роли из Access Token
						Set<GrantedAuthority> rolesFromToken = extractRolesFromAccessToken(accessToken);

						// Добавляем роли из токена
						mappedAuthorities.addAll(rolesFromToken);
					}
				} catch (Exception e) {
					log.error("Ошибка при извлечении ролей: {}", e.getMessage());
				}
			}

			return mappedAuthorities;
		};
	}

	@Bean
	public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		OidcUserService delegate = new OidcUserService();

		return (userRequest) -> {
			// Получаем стандартного пользователя из ID Token
			OidcUser oidcUser = delegate.loadUser(userRequest);

			// Получаем Access Token и извлекаем роли
			String accessToken = userRequest.getAccessToken().getTokenValue();
			Set<GrantedAuthority> rolesFromAccessToken = extractRolesFromAccessToken(accessToken);

			// Объединяем стандартные authority и роли из Access Token
			Set<GrantedAuthority> mergedAuthorities = new HashSet<>();
			mergedAuthorities.addAll(oidcUser.getAuthorities());
			mergedAuthorities.addAll(rolesFromAccessToken);

			// Создаем нового пользователя с объединенными ролями
			return new DefaultOidcUser(
					mergedAuthorities,
					oidcUser.getIdToken(),
					oidcUser.getUserInfo()
			);
		};
	}

	// Метод для извлечения ролей из Access Token
	private Set<GrantedAuthority> extractRolesFromAccessToken(String accessToken) {
		Set<GrantedAuthority> authorities = new HashSet<>();

		try {
			// Разделяем JWT на части
			String[] parts = accessToken.split("\\.");
			if (parts.length == 3) {
				// Декодируем payload (вторую часть)
				String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

				// Парсим JSON
				com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

				// 1. Извлекаем realm_access.roles (глобальные роли)
				Map<String, Object> realmAccess = (Map<String, Object>) payload.get("realm_access");
				if (realmAccess != null && realmAccess.containsKey("roles")) {
					List<String> roles = (List<String>) realmAccess.get("roles");
					for (String role : roles) {
						authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
					}
				}

				// 2. Извлекаем resource_access.{clientId}.roles (роли клиента)
				Map<String, Object> resourceAccess = (Map<String, Object>) payload.get("resource_access");
				if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
					Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
					List<String> roles = (List<String>) clientAccess.get("roles");
					if (roles != null) {
						for (String role : roles) {
							authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Ошибка при извлечении ролей из Access Token: " + e.getMessage());
		}

		return authorities;
	}

	@Bean
	public LogoutSuccessHandler keycloakLogoutSuccessHandler() {
		return (request, response, authentication) -> {
			String keycloakLogoutUrl = issuerUri + "/protocol/openid-connect/logout";

			String logoutUrl = UriComponentsBuilder
					.fromUriString(keycloakLogoutUrl)
					.queryParam("id_token_hint", getIdToken(authentication))
					.queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
					.queryParam("client_id", clientId)
					.build()
					.toUriString();

			response.sendRedirect(logoutUrl);
		};
	}

	private String getIdToken(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
			return oidcUser.getIdToken().getTokenValue();
		}
		return null;
	}
}
