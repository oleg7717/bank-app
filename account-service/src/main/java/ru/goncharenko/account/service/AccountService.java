package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.mapper.ClientMapper;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.bankclient.common.exception.NotFoundException;
import ru.goncharenko.bankclient.common.exception.NotificationServiceException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.ClientDto;
import ru.goncharenko.bankclient.common.model.ClientListDto;
import ru.goncharenko.bankclient.common.model.ClientModifyDto;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final SecurityUtils securityUtils;
	private final ClientRepository repository;
	private final ClientMapper clientMapper;
	private final NotificationSendService notificationSendService;

	@Setter
	@Value("${spring.application.name}")
	private String service;

	public Mono<ResponseEntity<ClientDto>> getClientData() {
		return securityUtils.getCurrentUsername()
				.flatMap(userName ->
						repository.findByLogin(userName)
								.map(clientMapper::mapToDto)
								.flatMap(account -> Mono.just(ResponseEntity.ok()
										.body(account)))
								.switchIfEmpty(
										Mono.error(new NotFoundException(
												String.format("У пользователя %s нет аккаунта в банке ", userName)
										))
								)
				);
	}

	@Transactional(noRollbackFor = NotificationServiceException.class)
	public Mono<ResponseEntity<ClientDto>> modifyPersonalData(Mono<ClientModifyDto> accountModifyDto, String login) {
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

							return repository.updateAccount(
											accountModify.getFirstname(),
											accountModify.getSurname(),
											accountModify.getBirthdate(),
											login)
									.flatMap(updated -> repository.findByLogin(login)
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
		).switchIfEmpty(
				Mono.error(new NotFoundException(
						String.format("У пользователя %s нет аккаунта в банке ", login)
				))
		);
	}

	public Flux<ClientListDto> getClientsListForTransfer() {
		return repository.findAll().map(clientMapper::mapToList);
	}
}
