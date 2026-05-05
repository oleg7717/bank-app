package ru.goncharenko.bankfront.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.goncharenko.bankclient.common.enums.CashAction;
import ru.goncharenko.bankclient.common.model.cashoperation.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsDto;
import ru.goncharenko.bankclient.web.config.utils.SecurityUtils;

@Component
@RequiredArgsConstructor
public class RequestParamToDtoMapper {
	private final SecurityUtils securityUtils;

	public DepositOrWithdrawDto depositOrWithdrawDto(String currency, int value, CashAction action) {
		String login = securityUtils.getCurrentUsername();
		return DepositOrWithdrawDto.builder()
				.login(login)
				.currency(currency)
				.amount((double) value)
				.action(action)
				.build();
	}

	public TransferBetweenAccountsDto transferBetweenClientsAccountsDto(
			String toAccount,
			String fromCurrency,
			String toCurrency,
			int value) {
		String login = securityUtils.getCurrentUsername();
		return TransferBetweenAccountsDto.builder()
				.fromAccount(login)
				.toAccount(toAccount)
				.fromCurrency(fromCurrency)
				.toCurrency(toCurrency)
				.amount((double) value)
				.build();
	}

	public TransferBetweenOwnAccountsDto transferCashBetweenOwnAccountsDto(
			String fromCurrency,
			String toCurrency,
			int value) {
		String login = securityUtils.getCurrentUsername();
		return TransferBetweenOwnAccountsDto.builder()
				.login(login)
				.fromCurrency(fromCurrency)
				.toCurrency(toCurrency)
				.amount((double) value)
				.build();
	}
}
