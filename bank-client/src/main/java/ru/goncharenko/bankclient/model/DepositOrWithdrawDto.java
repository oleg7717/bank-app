package ru.goncharenko.bankclient.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goncharenko.bankclient.enums.CashAction;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositOrWithdrawDto {
	private String login;
	private Double balance;
	private CashAction action;
}
