package ru.goncharenko.bankclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goncharenko.bankclient.enums.CashAction;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositOrWithdrawDto {
	private String login;
	private Double balance;
	private CashAction action;
}
