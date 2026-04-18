package ru.goncharenko.bankfront.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.goncharenko.bankclient.common.enums.CashAction;
import ru.goncharenko.bankfront.service.FrontAccountService;
import ru.goncharenko.bankfront.service.FrontCashService;
import ru.goncharenko.bankfront.service.FrontTransferService;
import ru.goncharenko.bankfront.service.RefreshService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class FrontController {
	private final FrontAccountService accountService;
	private final FrontCashService cashService;
	private final FrontTransferService transferService;
	private final RefreshService refreshService;

	@GetMapping
	public String index() {
		return "redirect:/account";
	}

	@GetMapping("/account")
	public ModelAndView getAccount() {
		return refreshService.getPageData();
	}

	@PostMapping("/account")
	public ModelAndView editAccount(
			@RequestParam("name") String name,
			@RequestParam("birthdate") LocalDate birthdate) {
		return refreshService.refreshPage(accountService::modifyAccount, name, birthdate);
	}

	@PostMapping("/cash")
	public ModelAndView editCash(
			@RequestParam("value") int value,
			@RequestParam("action") CashAction action) {
		return refreshService.refreshPage(cashService::depositOrWithdraw, value, action);
	}

	@PostMapping("/transfer")
	public ModelAndView transfer(
			@RequestParam("value") int value,
			@RequestParam("login") String login) {
		return refreshService.refreshPage(transferService::transferCash, value, login);
	}
}
