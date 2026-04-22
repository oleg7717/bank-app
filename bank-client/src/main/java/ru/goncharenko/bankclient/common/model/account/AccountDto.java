package ru.goncharenko.bankclient.common.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
	private String status;
	private Boolean main;
	private Double balance;
	private String currency;
}
