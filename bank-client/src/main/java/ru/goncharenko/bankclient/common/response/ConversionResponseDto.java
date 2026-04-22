package ru.goncharenko.bankclient.common.response;

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
public class ConversionResponseDto {
	private String fromCurrency;
	private String toCurrency;
	private Double originalAmount;
	private Double convertedAmount;
}
