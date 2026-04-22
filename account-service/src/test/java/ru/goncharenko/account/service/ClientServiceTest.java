package ru.goncharenko.account.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.goncharenko.account.mapper.ClientMapper;
import ru.goncharenko.account.model.Client;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.bankclient.common.exception.NotFoundException;
import ru.goncharenko.bankclient.common.exception.NotificationServiceException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.client.ClientDto;
import ru.goncharenko.bankclient.common.model.client.ClientListDto;
import ru.goncharenko.bankclient.common.model.client.ClientModifyDto;
import ru.goncharenko.bankclient.reactive.service.NotificationSendService;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

	@Mock
	private SecurityUtils securityUtils;

	@Mock
	private ClientRepository repository;

	@Mock
	private ClientMapper clientMapper;

	@Mock
	private NotificationSendService notificationSendService;

	@InjectMocks
	private AccountService accountService;

	private Client testClient;
	private ClientDto testClientDto;
	private ClientListDto testClientListDto;
	private ClientModifyDto testClientModifyDto;
	private final String TEST_LOGIN = "o.goncharenko";
	private final String SERVICE_NAME = "account-service";

	@BeforeEach
	void setUp() {
		testClient = Client.builder()
				.login(TEST_LOGIN)
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.balance(1000.0)
				.build();

		testClientDto = ClientDto.builder()
				.login(TEST_LOGIN)
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.balance(1000.0)
				.build();

		testClientListDto = ClientListDto.builder()
				.login(TEST_LOGIN)
				.name("Oleg Goncharenko")
				.build();

		testClientModifyDto = ClientModifyDto.builder()
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.build();

		accountService.setService(SERVICE_NAME);
	}

	@Test
	void getAccount_Success() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testClient));
		when(clientMapper.mapToDto(testClient)).thenReturn(testClientDto);

		StepVerifier.create(accountService.getClientData())
				.expectNextMatches(response ->
						response.getStatusCode() == HttpStatus.OK &&
								response.getBody() != null &&
								response.getBody().getLogin().equals(TEST_LOGIN)
				)
				.verifyComplete();

		verify(securityUtils).getCurrentUsername();
		verify(repository).findByLogin(TEST_LOGIN);
		verify(clientMapper).mapToDto(testClient);
	}

	@Test
	void getAccount_UserNotFound_ThrowsNotFoundException() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.empty());

		StepVerifier.create(accountService.getClientData())
				.expectErrorMatches(throwable ->
						throwable instanceof NotFoundException &&
								throwable.getMessage().contains("нет аккаунта")
				)
				.verify();

		verify(securityUtils).getCurrentUsername();
		verify(repository).findByLogin(TEST_LOGIN);
	}

	@Test
	void getAccount_WhenUsernameIsAnonymous_ShouldReturnNotFound() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just("anonymous"));
		when(repository.findByLogin("anonymous")).thenReturn(Mono.empty());

		StepVerifier.create(accountService.getClientData())
				.expectErrorMatches(throwable ->
						throwable instanceof NotFoundException &&
								throwable.getMessage().contains("нет аккаунта")
				)
				.verify();

		verify(securityUtils).getCurrentUsername();
		verify(repository).findByLogin("anonymous");
	}


	@Test
	void modifyAccount_UserUnder18_ThrowsValidationException() {
		ClientModifyDto underageDto = ClientModifyDto.builder()
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.now().minusYears(17))
				.build();

		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testClient));

		StepVerifier.create(accountService.modifyPersonalData(Mono.just(underageDto), TEST_LOGIN))
				.expectErrorMatches(throwable ->
						throwable instanceof ValidationException &&
								throwable.getMessage().contains("не может быть младше 18 лет")
				)
				.verify();

		verify(repository, never()).updateAccount(any(), any(), any(), any());
		verify(notificationSendService, never()).sendNotification(any(), any());
	}

	@Test
	void modifyAccount_UnauthorizedUser_ThrowsResponseStatusException() {
		String otherUser = "otherUser";
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(otherUser));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testClient));

		StepVerifier.create(accountService.modifyPersonalData(Mono.just(testClientModifyDto), TEST_LOGIN))
				.expectErrorMatches(throwable ->
						throwable instanceof ResponseStatusException &&
								((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
								throwable.getMessage().contains("не может менять данные другого аккаунта")
				)
				.verify();

		verify(repository, never()).updateAccount(any(), any(), any(), any());
	}

	@Test
	void modifyAccount_AccountNotFound_ThrowsNotFoundException() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.empty());

		StepVerifier.create(accountService.modifyPersonalData(Mono.just(testClientModifyDto), TEST_LOGIN))
				.expectErrorMatches(throwable ->
						throwable instanceof NotFoundException &&
								throwable.getMessage().contains("нет аккаунта")
				)
				.verify();

		verify(repository, never()).updateAccount(any(), any(), any(), any());
	}

	@Test
	void modifyAccount_NotificationFails_StillReturnsSuccessAndThrowsNotificationException() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testClient));
		when(repository.updateAccount(anyString(), anyString(), any(LocalDate.class), eq(TEST_LOGIN)))
				.thenReturn(Mono.just(1));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testClient));
		when(clientMapper.mapToDto(testClient)).thenReturn(testClientDto);

		when(notificationSendService.sendNotification(eq(SERVICE_NAME), anyString()))
				.thenReturn(Mono.error(new RuntimeException("Notification service down")));

		StepVerifier.create(accountService.modifyPersonalData(Mono.just(testClientModifyDto), TEST_LOGIN))
				.expectErrorMatches(throwable ->
						throwable instanceof NotificationServiceException &&
								throwable.getMessage().contains("Client updated, but notification send failed")
				)
				.verify();

		verify(repository).updateAccount(any(), any(), any(), eq(TEST_LOGIN));
		verify(notificationSendService).sendNotification(eq(SERVICE_NAME), eq("Персональные данные обновлены успешно"));
	}

	@Test
	void getAllAccounts_Success() {
		Client secondClient = Client.builder()
				.login("user2")
				.firstname("Hugh")
				.surname("Jackman")
				.balance(500.0)
				.build();

		when(repository.findAll()).thenReturn(Flux.just(testClient, secondClient));
		when(clientMapper.mapToList(testClient)).thenReturn(testClientListDto);

		ClientListDto secondListDto = ClientListDto.builder()
				.login("user2")
				.name("Hugh Jackman")
				.build();
		when(clientMapper.mapToList(secondClient)).thenReturn(secondListDto);

		StepVerifier.create(accountService.getClientsListForTransfer())
				.expectNext(testClientListDto)
				.expectNext(secondListDto)
				.verifyComplete();

		verify(repository).findAll();
		verify(clientMapper, times(2)).mapToList(any(Client.class));
	}
}
