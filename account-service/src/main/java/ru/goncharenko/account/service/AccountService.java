package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.exception.NotFoundException;
import ru.goncharenko.account.exception.ValidationException;
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository repository;
	private final AccountMapper accountMapper;
	String myLogin = "o.goncharenko";

	public Mono<ResponseEntity<AccountDto>> getAccount() {
		return repository.findByLogin(myLogin)
				.map(accountMapper::mapToDto)
				.flatMap(account -> Mono.just(ResponseEntity.ok()
						.body(account)))
				.switchIfEmpty(
						Mono.error(new NotFoundException(
								String.format("У пользователя %s нет аккаунта в банке ", myLogin)
						))
				);
	}

	public Mono<ResponseEntity<AccountDto>> modifyAccount(Mono<AccountModifyDto> accountModifyDto, String login) {
		return repository.findByLogin(login).flatMap(account ->
				accountModifyDto.flatMap(accountModify -> {
							if (!Objects.equals(account.getLogin(), login)) {
								return Mono.error(new ResponseStatusException(
										HttpStatus.BAD_REQUEST,
										String.format("Пользователь %s не может менять данные другого аккаунта", login)
								));
							}
							if (accountModify.getBirthdate().until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
								return Mono.error(new ValidationException(
										"Пользователь не может быть младше 18 лет"
								));
							}

							return repository.updateAccount(
											accountModify.getFirstname(),
											accountModify.getSurname(),
											accountModify.getBirthdate(),
											login)
									.flatMap(updated -> repository.findByLogin(login)
											.map(accountMapper::mapToDto)
											.flatMap(accountDto -> Mono.just(ResponseEntity.ok()
													.body(accountDto)))
									);
						}

				)
		).switchIfEmpty(
				Mono.error(new NotFoundException(
						String.format("У пользователя %s нет аккаунта в банке ", login)
				))
		);
	}

	public Flux<AccountDto> getAllAccounts() {
		return repository.findAll().map(accountMapper::mapToDto);
	}
}
