package ru.goncharenko.bankfront.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.config.RestClientService;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;
import ru.goncharenko.bankclient.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@Service
public class FrontAccountService {
	private final String login = "o.goncharenko";
	private final String accountBaseUrl;

	private final RestClientService restClient;

	public FrontAccountService(@Value("${application.service.account.url:http://localhost:8081}") String accountUrl, final RestClientService restClient) {
		this.accountBaseUrl = accountUrl + ACCOUNT_BASE_URL;
		this.restClient = restClient;
	}

	public AccountDto getAccount() {
		try {
			return restClient.getForObject(accountBaseUrl, AccountDto.class);
		} catch (RestClientException e) {
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
		}
	}

	public List<AccountListDto> getAccountsForTransfer() {
		try {
			return restClient.getForObject(accountBaseUrl + ACCOUNT_LIST, new ParameterizedTypeReference<>() {});
		} catch (RestClientException e) {
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
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
			AccountModifyDto modifyDto = new AccountModifyDto(firstname, surname, birthdate);
			restClient.postForObject(accountBaseUrl + "/" + login, modifyDto, AccountDto.class);
		} catch (RestClientException e) {
			// Обработка ошибок RestClient
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
		}
	}

	public ModelAndView fillModel(AccountDto account, List<AccountListDto> accountList) {
		ModelAndView model = new ModelAndView("main");

		if (account != null) {
			model.addObject("name", account.getSurname() + " " + account.getFirstname());
			model.addObject("birthdate", account.getBirthdate());
			model.addObject("sum", account.getBalance());
		}

		if (accountList != null) {
			model.addObject("accounts", accountList);
		}

		return model;
	}

	public ModelAndView fillModelWithError(AccountDto account, List<AccountListDto> accountList, List<String> errors) {
		ModelAndView model = fillModel(account, accountList);
//		ModelAndView model = new ModelAndView("main");
		model.addObject("errors", errors);

		return model;
	}
}
