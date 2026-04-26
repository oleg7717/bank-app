package ru.goncharenko.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.goncharenko.exchange.model.CurrencyRate;

import java.util.List;
import java.util.Optional;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
	Optional<CurrencyRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);

	List<CurrencyRate> findAllByFromCurrency(String fromCurrency);
}
