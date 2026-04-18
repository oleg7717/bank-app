package ru.goncharenko.bankclient.common.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class AntifraudDto {
	@NotNull
	private String fromAccount;

	private String toAccount;

	@NotNull
	private Boolean betweenOwnAccounts;

	@NotNull
	@Positive
	private Double amount;

	public Boolean isBetweenAccountTransfer() {
		return betweenOwnAccounts;
	}
}
