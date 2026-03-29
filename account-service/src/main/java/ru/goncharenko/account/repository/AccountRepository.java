package ru.goncharenko.account.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.entity.Account;

import java.time.LocalDate;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
	Mono<Account> findByLogin(String userName);

	@Modifying
	@Query("UPDATE accounts SET firstname = COALESCE(:firstname, firstname), " +
			"surname = COALESCE(:surname, surname), " +
			"birthdate = COALESCE(:birthdate, birthdate) " +
			"WHERE login = :login")
	Mono<Integer> updatePartial(String firstname, String surname, LocalDate birthdate, String login);
}
