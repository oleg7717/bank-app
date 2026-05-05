package ru.goncharenko.account.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.entity.Account;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
	Mono<Account> getAccountByClientIdAndCurrency(Long clientId, String currency);

	Flux<Account> getAccountsByClientId(Long clientId);

	Flux<Object> getAccountsByClientIdAndBalanceIsGreaterThan(Long clientId, Double balanceIsGreaterThan);

	@Modifying
	@Query("UPDATE accounts SET balance = COALESCE(:balance, balance) " +
			"WHERE login = :login and currency = :currency")
	Mono<Integer> updateBalance(Double balance, String login, String currency);

	@Modifying
	@Query("UPDATE accounts SET balance = balance - :balance " +
			"WHERE login = :fromLogin and currency = :currency")
	Mono<Integer> transferCashFrom(Double balance, String fromLogin, String currency);

	@Modifying
	@Query("UPDATE accounts SET balance = balance + :balance " +
			"WHERE login = :toLogin and currency = :currency")
	Mono<Integer> transferCashTo(Double balance, String toLogin, String currency);
}
