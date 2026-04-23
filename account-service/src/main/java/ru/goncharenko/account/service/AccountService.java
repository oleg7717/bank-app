package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.exception.AccountAlreadyExist;
import ru.goncharenko.account.exception.AccountException;
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.mapper.ClientMapper;
import ru.goncharenko.account.model.Account;
import ru.goncharenko.account.model.Client;
import ru.goncharenko.account.model.enums.AccountStatus;
import ru.goncharenko.account.model.enums.ClientStatus;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.account.utils.Generator;
import ru.goncharenko.bankclient.common.exception.NotFoundException;
import ru.goncharenko.bankclient.common.exception.NotificationServiceException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.account.AccountDto;
import ru.goncharenko.bankclient.common.model.account.CreateAccountDto;
import ru.goncharenko.bankclient.common.model.client.ClientDto;
import ru.goncharenko.bankclient.common.model.client.ClientListDto;
import ru.goncharenko.bankclient.common.model.client.ClientModifyDto;
import ru.goncharenko.bankclient.common.model.client.CreateClientDto;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final ClientRepository clientRepository;
	private final AccountRepository accountRepository;

	private final ClientMapper clientMapper;
	private final AccountMapper accountMapper;

	private final NotificationSendService notificationSendService;

	private final TransactionalOperator transactionalOperator;
	private final SecurityUtils securityUtils;
	private final Generator generator;

	@Setter
	@Value("${spring.application.name}")
	private String service;

	@Transactional
	public Mono<ResponseEntity<ClientDto>> createClient(Mono<CreateClientDto> clientDto) {
		return clientDto
				.flatMap(dto -> {
					String login = generator.generateLogin(dto.getFirstname(), dto.getSurname());
					return clientRepository.findByLogin(login)
							.flatMap(existingClient ->
									Mono.just(ResponseEntity.ok(clientMapper.mapToDto(existingClient)))
							)
							.switchIfEmpty(
									Mono.defer(() -> {
										if (dto.getBirthdate().until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
											return Mono.error(new ValidationException(
													"Пользователь не может быть младше 18 лет"
											));
										}
										Client newClient = clientMapper.mapToEntity(dto);
										newClient.setStatus(ClientStatus.ACTIVE);
										newClient.setLogin(login);
										return clientRepository.save(newClient)
												.flatMap(savedClient -> {
													Account account = Account.builder()
															.clientId(savedClient.getId())
															.status(AccountStatus.ACTIVE)
															.main(true)
															.balance(0.0)
															.currency("RUB")
															.build();
													return accountRepository.save(account)
															.thenReturn(
																	ResponseEntity
																			.status(HttpStatus.CREATED)
																			.body(clientMapper.mapToDto(savedClient))
															);
												});
									})
							);
				})
				.as(transactionalOperator::transactional);
	}

	public Mono<ResponseEntity<ClientDto>> getClientData() {
		return securityUtils.getCurrentUsername()
				.flatMap(username ->
						clientRepository.findByLoginAndStatus(username, ClientStatus.ACTIVE)
								.map(clientMapper::mapToDto)
								.flatMap(account -> Mono.just(ResponseEntity.ok()
										.body(account)))
								.switchIfEmpty(noAccount(username))
				);
	}

	@Transactional(noRollbackFor = NotificationServiceException.class)
	public Mono<ResponseEntity<ClientDto>> modifyPersonalData(Mono<ClientModifyDto> accountModifyDto, String login) {
		return securityUtils.getCurrentUsername().flatMap(userName ->
				clientRepository.findByLoginAndStatus(login, ClientStatus.ACTIVE).flatMap(account ->
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

									return clientRepository.updateAccount(
													accountModify.getFirstname(),
													accountModify.getSurname(),
													accountModify.getBirthdate(),
													login)
											.flatMap(updated -> clientRepository.findByLogin(login)
													.map(clientMapper::mapToDto)
													.flatMap(accountDto ->
															notificationSendService.sendNotification(
																			service,
																			"Персональные данные обновлены успешно"
																	)
																	.thenReturn(ResponseEntity.ok().body(accountDto))
																	.onErrorResume(error ->
																			// Данные сохранены, но возвращаем ошибку уведомления
																			Mono.error(new NotificationServiceException(
																					"Client updated, but notification send failed"
																			))
																	)
													)
											);
								}

						))
		).switchIfEmpty(noAccount(login));
	}

	public Flux<ClientListDto> getClientsListForTransfer() {
		return clientRepository.findClientsByStatus(ClientStatus.ACTIVE).map(clientMapper::mapToList);
	}

	public Mono<ResponseEntity<AccountDto>> createAccount(Mono<CreateAccountDto> accountDto) {
		return securityUtils.getCurrentUsername()
				.flatMap(username -> clientRepository.findByLoginAndStatus(username, ClientStatus.ACTIVE)
						.switchIfEmpty(noAccount(username))
						.flatMap(client -> accountDto
								.flatMap(dto ->
										accountRepository.getAccountByClientIdAndCurrency(
														client.getId(),
														dto.getCurrency()
												)
												.flatMap(account -> Mono.<ResponseEntity<AccountDto>>error(
														new AccountAlreadyExist(String.format(
																"У пользователя %s уже есть счёт в валюте: %s",
																username,
																account.getCurrency()
														))
												))
												.switchIfEmpty(Mono.defer(() -> {
													Account newAccount = Account.builder()
															.clientId(client.getId())
															.status(AccountStatus.ACTIVE)
															.main(false)
															.balance(0.0)
															.currency(dto.getCurrency())
															.build();
													return accountRepository.save(newAccount)
															.map(savedAccount -> ResponseEntity
																	.status(HttpStatus.CREATED)
																	.body(accountMapper.mapEntityToDto(savedAccount))
															);
												}))
								)
						)
				);
	}

	public Mono<ResponseEntity<Void>> deleteClient(String login) {
		return securityUtils.getCurrentUsername().flatMap(username -> {
			if (!username.equals(login)) {
				return Mono.error(new NotFoundException(
						String.format("Пользователь %s не может удалить аккаунт другого пользователя", username)
				));
			}

			return clientRepository.findByLoginAndStatus(login, ClientStatus.ACTIVE)
					.switchIfEmpty(noAccount(username))
					.flatMap(client ->
							accountRepository.getAccountsByClientIdAndBalanceIsGreaterThan(client.getId(), 0.0)
									.hasElements()
									.flatMap(hasAccountsWithBalance -> {
										if (hasAccountsWithBalance) {
											return Mono.error(new AccountException(
													String.format("У пользователя %s есть аккаунты с положительным балансом", login)
											));
										}

										client.setStatus(ClientStatus.DISABLED);
										return clientRepository.save(client)
												.then(accountRepository.getAccountsByClientId(client.getId())
														.collectList()
														.flatMap(accounts -> {
															for (Account account : accounts) {
																account.setStatus(AccountStatus.DISABLED);
															}
															return accountRepository.saveAll(accounts)
																	.then()
																	.thenReturn(ResponseEntity
																			.status(HttpStatus.ACCEPTED)
																			.body(null));
														})
												);
									})
					);
		});
	}

	private <T> Mono<T> noAccount(String username) {
		return Mono.error(
				new NotFoundException(String.
						format("У пользователя %s нет аккаунта в банке ", username))
		);
	}

/*
	переводить деньги между своими счетами с учётом конвертации в различные валюты;
	переводить деньги на другой счёт с учётом конвертации в различные валюты.
	списка счетов пользователя с возможностью удаления (у пользователя может быть не более одного счёта в определённой валюте);

	Блок внесения и снятия виртуальных денег
	Состоит из:
		поля выбора счёта (обязательно);

	Блок перевода между своими счетами
	Состоит из:
		поля выбора своего счёта для отправки денег (обязательно);
		поля выбора своего счёта для получения (обязательно);
		поля ввода суммы перевода (если сумма больше суммы на счёте отправления, то должна появляться ошибка);
		кнопки, при нажатии на которую осуществляется перевод денег.

	Блок перевода денег на счёт другого аккаунта
	Состоит из:
        поля выбора своего счёта для отправки денег (обязательно);
        поля выбора счёта получателя (обязательно, с поиском по аккаунту);
        поля ввода суммы перевода (если сумма больше суммы на счёте отправления, то должна появляться ошибка);
        кнопки, при нажатии на которую осуществляется перевод денег.

	Блок курсов валют
        Первый столбец таблицы — валюта.
        Второй столбец — покупка.
        Третий столбец — продажа.
*/
}
