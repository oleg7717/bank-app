package ru.goncharenko.bankfront.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDTO;

import java.util.List;

import static ru.goncharenko.bankclient.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.endpoint.Endpoints.ACCOUNT_LIST;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final RestClient restClient;

	public AccountDto getAccount() {
		try {
			return restClient.get()
					.uri(ACCOUNT_BASE_URL)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> System.err.println("Custom 4xx handler: " + response.getStatusCode()))
					.body(AccountDto.class);
		} catch (RestClientException e) {
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
		}
	}

	public List<AccountListDTO> getAccountsForTransfer() {
		try {
			return restClient.get()
					.uri(ACCOUNT_BASE_URL + ACCOUNT_LIST)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> System.err.println("Custom 4xx handler: " + response.getStatusCode()))
					.body(new ParameterizedTypeReference<>() {
					});
		} catch (RestClientException e) {
			// Обработка ошибок RestClient
			System.err.println("RestClient error: " + e.getMessage());

			throw e;
		}
	}

	public ModelAndView fillModel(AccountDto account, List<AccountListDTO> ModelAccountDTO) {
		ModelAndView model = new ModelAndView("main");
		model.addObject("name", account.getFirstname() + " " + account.getSurname());
		model.addObject("birthdate", account.getBirthdate());
		model.addObject("sum", account.getBalance());
		model.addObject("accounts", ModelAccountDTO);

		return model;
	}

	private ModelAndView fillModelWithError(String error) {
		ModelAndView model = new ModelAndView("main");
		model.addObject("errors", List.of(error));

		return model;
	}
}
