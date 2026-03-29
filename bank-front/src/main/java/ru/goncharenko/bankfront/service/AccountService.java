package ru.goncharenko.bankfront.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.model.AccountDto;

import java.time.format.DateTimeFormatter;

import static ru.goncharenko.bankclient.endpoint.Endpoints.ACCOUNT_BASE_URL;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final RestClient restClient;

	public ModelAndView getAccount() {
		AccountDto accountDto = restClient.get()
				.uri(ACCOUNT_BASE_URL)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
					System.err.println("Custom 4xx handler: " + response.getStatusCode());
				})
				.body(AccountDto.class);

		return fillModel(accountDto);
	}

	private ModelAndView fillModel(AccountDto account) {
		ModelAndView model = new ModelAndView("main");
		model.addObject("name", account.getFirstname() + " " + account.getSurname());
		model.addObject("birthdate", account.getBirthdate().format(DateTimeFormatter.ISO_DATE));
		model.addObject("sum", account.getBalance());

		return model;
	}
}
