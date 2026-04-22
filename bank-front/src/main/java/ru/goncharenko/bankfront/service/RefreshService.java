package ru.goncharenko.bankfront.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.client.ClientDto;
import ru.goncharenko.bankclient.common.model.client.ClientListDto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class RefreshService {
	private final FrontAccountService accountService;

	public <T, R> ModelAndView refreshPage(BiConsumer<T, R> consumer, T param1, R param2) {
		List<String> errors = new ArrayList<>();
		try {
			consumer.accept(param1, param2);
		} catch (ValidationException | RestClientException ex) {
			errors.add(ex.getMessage());
		}

		ModelAndView model = getPageData();

		if (!errors.isEmpty()) {
			model.addObject("errors", errors);
		}

		return model;
	}

	public ModelAndView getPageData() {
		ClientDto account = null;
		List<ClientListDto> accountList = null;
		List<String> errors = new ArrayList<>();

		try {
			accountList = accountService.getAccountsForTransfer();
		} catch (RestClientException ex) {
			errors.add(ex.getMessage());
		}

		try {
			account = accountService.getAccount();
		} catch (RestClientException ex) {
			errors.add(ex.getMessage());
		}

		return fillModel(account, accountList, errors);
	}

	private ModelAndView fillModel(ClientDto account, List<ClientListDto> accountList, List<String> errors) {
		ModelAndView model = new ModelAndView("main");

		if (account != null) {
			model.addObject("name", account.getSurname() + " " + account.getFirstname());
			model.addObject("birthdate", account.getBirthdate());
			model.addObject("sum", account.getBalance());
		}

		if (accountList != null) {
			model.addObject("accounts", accountList);
		}

		if (errors != null && !errors.isEmpty()) {
			model.addObject("errors", errors);
		}

		return model;
	}
}
