package ru.goncharenko.account.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.entity.Client;
import ru.goncharenko.account.model.enums.ClientStatus;

import java.time.LocalDate;

public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {
	Mono<Client> findByLogin(String userName);

	@Modifying
	@Query("UPDATE clients SET firstname = COALESCE(:firstname, firstname), " +
			"surname = COALESCE(:surname, surname), " +
			"birthdate = COALESCE(:birthdate, birthdate) " +
			"WHERE login = :login")
	Mono<Integer> updateAccount(String firstname, String surname, LocalDate birthdate, String login);

	Mono<Client> findByLoginAndStatus(String login, ClientStatus status);

	Flux<Client> findClientsByStatus(ClientStatus status);
}
