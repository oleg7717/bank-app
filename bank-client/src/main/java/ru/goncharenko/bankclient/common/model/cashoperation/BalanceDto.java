package ru.goncharenko.bankclient.common.model.cashoperation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {
	private Double amount;
}
