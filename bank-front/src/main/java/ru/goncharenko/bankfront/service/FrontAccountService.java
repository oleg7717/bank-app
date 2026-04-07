package ru.goncharenko.bankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.service.RestClientService;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;
import ru.goncharenko.bankclient.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Slf4j
@Service
public class FrontAccountService {
	private final String login = "o.goncharenko";
	private final String accountBaseUrl;

	private final RestClientService restClient;

	public FrontAccountService(@Value("${application.service.account.url:http://localhost:8081}") String accountUrl,
	                           final RestClientService restClient) {
		this.accountBaseUrl = accountUrl + ACCOUNT_BASE_URL;
		this.restClient = restClient;
	}

	@PreAuthorize("hasRole('account_editor')")
	public AccountDto getAccount() {
		try {
			return restClient.getForObject(accountBaseUrl, AccountDto.class);
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}

	public List<AccountListDto> getAccountsForTransfer() {
		try {
			return restClient.getForObject(accountBaseUrl + ACCOUNT_LIST, new ParameterizedTypeReference<>() {});
		} catch (RestClientException ex) {
			log.error("RestClient error: {}", ex.getMessage());

			throw ex;
		}
	}

	public void modifyAccount(String name, LocalDate birthdate) {
		if (birthdate.until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
			throw new ValidationException("Пользователь не может быть младше 18 лет");
		}
		try {
			String[] usernameArray = name.split(" ");
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
