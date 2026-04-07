package ru.goncharenko.bankclient.model;

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
public class TransferCashDto {
	private String fromAccount;
	private String toAccount;
	private Double amount;
}
