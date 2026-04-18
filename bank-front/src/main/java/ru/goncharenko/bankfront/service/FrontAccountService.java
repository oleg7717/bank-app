package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.common.model.AccountDto;
import ru.goncharenko.bankclient.common.model.AccountListDto;
import ru.goncharenko.bankclient.common.model.AccountModifyDto;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankfront.config.security.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.goncharenko.bankclient.common.endpoint.Endpoints.*;

@Slf4j
@Service
public class FrontAccountService {
	private final String accountBaseUrl;
	private final RestClientService restClient;
	private final SecurityUtils securityUtils;

	public FrontAccountService(@Value("${application.service.gateway.url:http://localhost:9080}") String gatewayUrl,
	                           final RestClientService restClient,
	                           SecurityUtils securityUtils) {
		this.accountBaseUrl = gatewayUrl + ACCOUNT_GATEWAY + ACCOUNT_BASE_URL;
		this.restClient = restClient;
		this.securityUtils = securityUtils;
	}

	@PreAuthorize("hasRole('account_viewer')")
	public AccountDto getAccount() {
		try {
			return restClient.getForObject(accountBaseUrl, AccountDto.class);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}

	@PreAuthorize("hasRole('account_viewer')")
	public List<AccountListDto> getAccountsForTransfer() {
		try {
			return restClient.getForObject(accountBaseUrl + ACCOUNT_LIST, new ParameterizedTypeReference<>() {});
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}

	@PreAuthorize("hasRole('account_editor')")
	public void modifyAccount(String name, LocalDate birthdate) {
		String login = securityUtils.getCurrentUsername();
		if (birthdate.until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
			throw new ValidationException("Пользователь не может быть младше 18 лет");
		}
		try {
			String[] usernameArray = name.split(" ");
			if (usernameArray.length < 2) {
				throw new ValidationException("В запросе должно быть имя и фамилия");
			}
			String surname = usernameArray[0];
			String firstname = usernameArray[1];
			AccountModifyDto modifyDto = AccountModifyDto.builder()
					.firstname(firstname)
					.surname(surname)
					.birthdate(birthdate)
					.build();
			restClient.postForObject(accountBaseUrl + "/" + login, modifyDto, AccountDto.class);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}
}
