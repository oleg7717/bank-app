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
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.model.entity.Account;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.bankclient.exception.NotFoundException;
import ru.goncharenko.bankclient.exception.NotificationServiceException;
import ru.goncharenko.bankclient.exception.ValidationException;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;
import ru.goncharenko.bankclient.service.NotificationSendService;
import ru.goncharenko.bankclient.utils.SecurityUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

	@Mock
	private SecurityUtils securityUtils;

	@Mock
	private AccountRepository repository;

	@Mock
	private AccountMapper accountMapper;

	@Mock
	private NotificationSendService notificationSendService;

	@InjectMocks
	private AccountService accountService;

	private Account testAccount;
	private AccountDto testAccountDto;
	private AccountListDto testAccountListDto;
	private AccountModifyDto testAccountModifyDto;
	private final String TEST_LOGIN = "o.goncharenko";
	private final String SERVICE_NAME = "account-service";

	@BeforeEach
	void setUp() {
		testAccount = Account.builder()
				.login(TEST_LOGIN)
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.balance(1000.0)
				.build();

		testAccountDto = AccountDto.builder()
				.login(TEST_LOGIN)
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.balance(1000.0)
				.build();

		testAccountListDto = AccountListDto.builder()
				.login(TEST_LOGIN)
				.name("Oleg Goncharenko")
				.build();

		testAccountModifyDto = AccountModifyDto.builder()
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1992, 4, 1))
				.build();

		accountService.setService(SERVICE_NAME);
	}

	@Test
	void getAccount_Success() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testAccount));
		when(accountMapper.mapToDto(testAccount)).thenReturn(testAccountDto);

		StepVerifier.create(accountService.getAccount())
				.expectNextMatches(response ->
						response.getStatusCode() == HttpStatus.OK &&
								response.getBody() != null &&
								response.getBody().getLogin().equals(TEST_LOGIN)
				)
				.verifyComplete();

		verify(securityUtils).getCurrentUsername();
		verify(repository).findByLogin(TEST_LOGIN);
		verify(accountMapper).mapToDto(testAccount);
	}

	@Test
	void getAccount_UserNotFound_ThrowsNotFoundException() {
		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.empty());

		StepVerifier.create(accountService.getAccount())
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

		StepVerifier.create(accountService.getAccount())
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
		AccountModifyDto underageDto = AccountModifyDto.builder()
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.now().minusYears(17))
				.build();

		when(securityUtils.getCurrentUsername()).thenReturn(Mono.just(TEST_LOGIN));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testAccount));

		StepVerifier.create(accountService.modifyAccount(Mono.just(underageDto), TEST_LOGIN))
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
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testAccount));

		StepVerifier.create(accountService.modifyAccount(Mono.just(testAccountModifyDto), TEST_LOGIN))
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

		StepVerifier.create(accountService.modifyAccount(Mono.just(testAccountModifyDto), TEST_LOGIN))
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
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testAccount));
		when(repository.updateAccount(anyString(), anyString(), any(LocalDate.class), eq(TEST_LOGIN)))
				.thenReturn(Mono.just(1));
		when(repository.findByLogin(TEST_LOGIN)).thenReturn(Mono.just(testAccount));
		when(accountMapper.mapToDto(testAccount)).thenReturn(testAccountDto);

		when(notificationSendService.sendNotification(eq(SERVICE_NAME), anyString()))
				.thenReturn(Mono.error(new RuntimeException("Notification service down")));

		StepVerifier.create(accountService.modifyAccount(Mono.just(testAccountModifyDto), TEST_LOGIN))
				.expectErrorMatches(throwable ->
						throwable instanceof NotificationServiceException &&
								throwable.getMessage().contains("Account updated, but notification send failed")
				)
				.verify();

		verify(repository).updateAccount(any(), any(), any(), eq(TEST_LOGIN));
		verify(notificationSendService).sendNotification(eq(SERVICE_NAME), eq("Персональные данные обновлены успешно"));
	}

	@Test
	void getAllAccounts_Success() {
		Account secondAccount = Account.builder()
				.login("user2")
				.firstname("Hugh")
				.surname("Jackman")
				.balance(500.0)
				.build();

		when(repository.findAll()).thenReturn(Flux.just(testAccount, secondAccount));
		when(accountMapper.mapToList(testAccount)).thenReturn(testAccountListDto);

		AccountListDto secondListDto = AccountListDto.builder()
				.login("user2")
				.name("Hugh Jackman")
				.build();
		when(accountMapper.mapToList(secondAccount)).thenReturn(secondListDto);

		StepVerifier.create(accountService.getAllAccounts())
				.expectNext(testAccountListDto)
				.expectNext(secondListDto)
				.verifyComplete();

		verify(repository).findAll();
		verify(accountMapper, times(2)).mapToList(any(Account.class));
	}
}
