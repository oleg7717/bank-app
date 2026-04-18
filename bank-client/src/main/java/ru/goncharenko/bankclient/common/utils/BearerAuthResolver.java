package ru.goncharenko.bankclient.common.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Objects;

public class BearerAuthResolver {
	public static void setAuthHeader(HttpHeaders headers) {
		Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
		if (principal instanceof DefaultOidcUser user) {
			String bearerToken = user.getIdToken().getTokenValue();
			headers.setBearerAuth(bearerToken);
		}
	}
}
