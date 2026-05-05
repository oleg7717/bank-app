package ru.goncharenko.bankclient.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDto {
	private String fromCurrency;
	private String toCurrency;
	private Double rate;
	private LocalDateTime updatedAt;
}
