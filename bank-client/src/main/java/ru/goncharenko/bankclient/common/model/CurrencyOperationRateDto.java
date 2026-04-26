package ru.goncharenko.bankclient.common.model;

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
public class CurrencyOperationRateDto {
	private String currency;
	private Double buy;
	private Double sell;
}
