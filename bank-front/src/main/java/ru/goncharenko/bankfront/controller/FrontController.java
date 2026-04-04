package ru.goncharenko.bankfront.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.enums.CashAction;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;
import ru.goncharenko.bankfront.service.FrontAccountService;
import ru.goncharenko.bankclient.exception.ValidationException;
import ru.goncharenko.bankfront.service.FrontCashService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер main.html.
 *
 * Используемая модель для main.html:
 *      model.addAttribute("name", name);
 *      model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 *      model.addAttribute("sum", sum);
 *      model.addAttribute("accounts", accounts);
 *      model.addAttribute("errors", errors);
 *      model.addAttribute("info", info);
 *
 * Поля модели:
 *      name - Фамилия Имя текущего пользователя, String (обязательное)
 *      birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 *      sum - сумма на счету текущего пользователя, Integer (обязательное)
 *      accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 *      errors - список ошибок после выполнения действий, List<String> (не обязательное)
 *      info - строка успешности после выполнения действия, String (не обязательное)
 *
 * С примерами использования можно ознакомиться в тестовом классе заглушке AccountStub
 */
@Controller
@RequiredArgsConstructor
public class FrontController {
	private final FrontAccountService accountService;
	private final FrontCashService cashService;
	/**
	 * GET /.
	 * Редирект на GET /account
	 */
	@GetMapping
	public String index() {
		return "redirect:/account";
	}

	/**
	 * GET /account.
	 * Что нужно сделать:
	 * 1. Сходить в сервис accounts через Gateway API для получения данных аккаунта по REST
	 * 2. Заполнить модель main.html полученными из ответа данными
	 * 3. Текущего пользователя можно получить из контекста Security
	 */
	@GetMapping("/account")
	public ModelAndView getAccount() {
		return getPageData();
	}

	/**
	 * POST /account.
	 * Что нужно сделать:
	 * 1. Сходить в сервис accounts через Gateway API для изменения данных текущего пользователя по REST
	 * 2. Заполнить модель main.html полученными из ответа данными
	 * 3. Текущего пользователя можно получить из контекста Security
	 *
	 * Изменяемые данные:
	 * 1. name - Фамилия Имя
	 * 2. birthdate - дата рождения в формате YYYY-DD-MM
	 */
	@PostMapping("/account")
	public ModelAndView editAccount(
			@RequestParam("name") String name,
			@RequestParam("birthdate") LocalDate birthdate
	) {
		List<String> errors = new ArrayList<>();
		try {
			accountService.modifyAccount(name, birthdate);
		} catch (ValidationException | RestClientException ex) {
			errors.add(ex.getMessage());
		}

		ModelAndView model = getPageData();

		if (!errors.isEmpty()) {
			List<String> existErrors = (List<String>) model.getModel().get("errors");
			existErrors.addAll(errors);
			model.addObject("errors", errors);
		}

		return model;
	}

	/**
	 * POST /cash.
	 * Что нужно сделать:
	 * 1. Сходить в сервис cash через Gateway API для снятия/пополнения счета текущего аккаунта по REST
	 * 2. Заполнить модель main.html полученными из ответа данными
	 * 3. Текущего пользователя можно получить из контекста Security
	 *
	 * Параметры:
	 * 1. value - сумма списания
	 * 2. action - GET (снять), PUT (пополнить)
	 */
	@PostMapping("/cash")
	public ModelAndView editCash(
			@RequestParam("value") int value,
			@RequestParam("action") CashAction action
	) {
		List<String> errors = new ArrayList<>();
		try {
			cashService.depositOrWithdraw(value, action);
		} catch (ValidationException | RestClientException ex) {
			errors.add(ex.getMessage());
		}

		ModelAndView model = getPageData();

		if (!errors.isEmpty()) {
			List<String> existErrors = (List<String>) model.getModel().get("errors");
			existErrors.addAll(errors);
			model.addObject("errors", errors);
		}

		return model;
	}

	/**
	 * POST /transfer.
	 * Что нужно сделать:
	 * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
	 * 2. Заполнить модель main.html полученными из ответа данными
	 * 3. Текущего пользователя можно получить из контекста Security
	 *
	 * Параметры:
	 * 1. value - сумма списания
	 * 2. login - логин пользователя получателя
	 */
	@PostMapping("/transfer")
	public String transfer(
			@RequestParam("value") int value,
			@RequestParam("login") String login
	) {
		// TODO: Заменить на то, что описано в комментарии к методу
//		accountStub.transfer(model, value, login);

		return "main";
	}

	private ModelAndView getPageData() {
		AccountDto account = null;
		List<AccountListDto> accountList = null;
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

		if (!errors.isEmpty()) {
			return accountService.fillModelWithError(account, accountList, errors);
		}

		return accountService.fillModel(account, accountList);
	}
}
