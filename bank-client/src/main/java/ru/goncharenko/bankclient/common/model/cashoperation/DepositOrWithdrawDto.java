package ru.goncharenko.bankclient.common.model.cashoperation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goncharenko.bankclient.common.enums.CashAction;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositOrWithdrawDto {
	private String login;
	@Builder.Default
	private String currency = "RUB";
	private Double amount;
	private CashAction action;
}
