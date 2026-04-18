package ru.goncharenko.bankclient.web.config.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtils {

	public String getCurrentUsername() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null) {
				log.warn("No authentication found in context");
				return "anonymous";
			}

			Object principal = authentication.getPrincipal();
			String username;

			if (principal instanceof DefaultOidcUser user) {
				username = (String) user.getClaims().get("preferred_username");
			} else {
				username = principal != null ? principal.toString() : "unknown";
			}

			log.info("Current user: {}", username);
			return username;
		} catch (Exception e) {
			log.error("Error getting security context", e);
			return "anonymous";
		}
	}
}
