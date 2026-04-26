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
import reactor.core.publisher.Mono;
import ru.goncharenko.account.exception.AccountException;
import ru.goncharenko.account.mapper.ClientMapper;
import ru.goncharenko.account.model.entity.Account;
import ru.goncharenko.account.model.entity.Client;
import ru.goncharenko.account.model.enums.AccountStatus;
import ru.goncharenko.account.model.enums.ClientStatus;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.account.utils.CommonLogic;
import ru.goncharenko.account.utils.Generator;
import ru.goncharenko.bankclient.common.exception.NotFoundException;
import ru.goncharenko.bankclient.common.exception.NotificationServiceException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.client.ClientDto;
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
public class ClientService {
	private final ClientRepository clientRepository;
	private final AccountRepository accountRepository;

	private final ClientMapper clientMapper;

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
							.flatMap(existingClient -> {
										if (existingClient.getStatus().equals(ClientStatus.ACTIVE)) {
											return Mono.just(ResponseEntity.ok(clientMapper.mapToDto(existingClient)));
										} else {
											return Mono.error(new ValidationException(
													"Пользователь заблокирован, обратитесь в отделение банка"
											));
										}
									}
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
								.switchIfEmpty(CommonLogic.noAccount(username))
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
				).switchIfEmpty(CommonLogic.noAccount(login))
				.as(transactionalOperator::transactional);
	}

	public Mono<ResponseEntity<Void>> deleteClient(String login) {
		return securityUtils.getCurrentUsername().flatMap(username -> {
			if (!username.equals(login)) {
				return Mono.error(new NotFoundException(
						String.format("Пользователь %s не может удалить аккаунт другого пользователя", username)
				));
			}

			return clientRepository.findByLoginAndStatus(login, ClientStatus.ACTIVE)
					.switchIfEmpty(CommonLogic.noAccount(username))
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
																	.then();
														})
														.thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build())
												);
									})
					);
		});
	}
}
