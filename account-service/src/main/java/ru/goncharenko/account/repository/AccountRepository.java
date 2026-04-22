package ru.goncharenko.account.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.Account;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
	Mono<Account> getAccountByClientIdAndCurrency(Long clientId, String currency);
}
