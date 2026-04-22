package ru.goncharenko.exchange.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "currency_rates")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fromCurrency;

	@Column(nullable = false)
	private String toCurrency;

	@Column(nullable = false)
	private Double rate;

	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
