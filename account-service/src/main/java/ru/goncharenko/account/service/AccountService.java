package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.config.security.utils.SecurityUtils;
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.bankclient.exception.NotFoundException;
import ru.goncharenko.bankclient.exception.ValidationException;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;
import ru.goncharenko.bankclient.service.NotificationSendService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final SecurityUtils securityUtils;
	private final AccountRepository repository;
	private final AccountMapper accountMapper;
	private final NotificationSendService notificationSendService;

	@Value("${spring.application.name}")
	private String service;
//	String myLogin = "o.goncharenko";

	public Mono<ResponseEntity<AccountDto>> getAccount() {
		return securityUtils.getCurrentUsername()
				.flatMap(userName ->
						repository.findByLogin(userName)
								.map(accountMapper::mapToDto)
								.flatMap(account -> Mono.just(ResponseEntity.ok()
										.body(account)))
								.switchIfEmpty(
										Mono.error(new NotFoundException(
												String.format("У пользователя %s нет аккаунта в банке ", userName)
										))
								)
				);
	}

	@Transactional
	public Mono<ResponseEntity<AccountDto>> modifyAccount(Mono<AccountModifyDto> accountModifyDto, String login) {
		return securityUtils.getCurrentUsername().flatMap(userName ->
				repository.findByLogin(login).flatMap(account ->
				accountModifyDto.flatMap(accountModify -> {
							if (!Objects.equals(account.getLogin(), userName)) {
								return Mono.error(new ResponseStatusException(
										HttpStatus.BAD_REQUEST,
										String.format("Пользователь %s не может менять данные другого аккаунта", userName)
								));
							}
							if (accountModify.getBirthdate().until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
								return Mono.error(new ValidationException(
										"Пользователь не может быть младше 18 лет"
								));
							}

							notificationSendService.sendNotification(service, "Обновление персональных данных").subscribe();

							return repository.updateAccount(
											accountModify.getFirstname(),
											accountModify.getSurname(),
											accountModify.getBirthdate(),
											login)
									.flatMap(updated -> repository.findByLogin(login)
											.map(accountMapper::mapToDto)
											.flatMap(accountDto ->
												notificationSendService.sendNotification(
														service,
														"Персональные данные обновлены успешно"
												).thenReturn((ResponseEntity.ok().body(accountDto)))
														//.subscribe()
//												return Mono.just(ResponseEntity.ok().body(accountDto));
											)
									);
						}

				))
		).switchIfEmpty(
				Mono.error(new NotFoundException(
						String.format("У пользователя %s нет аккаунта в банке ", login)
				))
		);
	}

	public Flux<AccountListDto> getAllAccounts() {
		return repository.findAll().map(accountMapper::mapToList);
	}
}
