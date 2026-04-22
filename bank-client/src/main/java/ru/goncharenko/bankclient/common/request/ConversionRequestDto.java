package ru.goncharenko.bankclient.common.request;

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
public class ConversionRequestDto {
	@NotNull
	private String fromCurrency;

	@NotNull
	private String toCurrency;

	@NotNull
	@Positive
	private Double amount;
}
