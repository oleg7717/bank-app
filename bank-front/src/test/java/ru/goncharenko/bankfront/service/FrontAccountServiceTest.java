package ru.goncharenko.bankfront.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.model.client.ClientDto;
import ru.goncharenko.bankclient.common.model.client.ClientListDto;
import ru.goncharenko.bankclient.common.model.client.ClientModifyDto;
import ru.goncharenko.bankclient.web.service.RestClientService;
import ru.goncharenko.bankclient.web.config.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrontAccountServiceTest {

	@Mock
	private RestClientService restClient;

	@Mock
	private SecurityUtils securityUtils;

	@InjectMocks
	private FrontAccountService frontAccountService;

	private ClientDto testClientDto;
	private ClientListDto testClientListDto;
	private final String TEST_LOGIN = "o.goncharenko";
	private final String TEST_NAME = "Oleg Goncharenko";

	@BeforeEach
	void setUp() {
		testClientDto = ClientDto.builder()
				.login(TEST_LOGIN)
				.firstname("Oleg")
				.surname("Goncharenko")
				.birthdate(LocalDate.of(1990, 1, 1))
				.balance(1000.0)
				.build();

		testClientListDto = ClientListDto.builder()
				.login(TEST_LOGIN)
				.build();
	}

	@Test
	void getAccount_Success() {
		when(restClient.getForObject(anyString(), eq(ClientDto.class)))
				.thenReturn(testClientDto);

		ClientDto result = frontAccountService.getAccount();

		assertNotNull(result);
		assertEquals(TEST_LOGIN, result.getLogin());
		assertEquals("Oleg", result.getFirstname());
		assertEquals("Goncharenko", result.getSurname());
		assertEquals(1000.0, result.getBalance());

		verify(restClient).getForObject(anyString(), eq(ClientDto.class));
	}

	@Test
	void getAccount_RestClientException_ThrowsException() {
		when(restClient.getForObject(anyString(), eq(ClientDto.class)))
				.thenThrow(new RestClientException("Service unavailable"));

		assertThrows(RestClientException.class, () -> frontAccountService.getAccount());

		verify(restClient).getForObject(anyString(), eq(ClientDto.class));
	}

	@Test
	void getAccountsForTransfer_Success() {
		List<ClientListDto> expectedList = List.of(testClientListDto);

		when(restClient.getForObject(anyString(), any(ParameterizedTypeReference.class)))
				.thenReturn(expectedList);

		List<ClientListDto> result = frontAccountService.getAccountsForTransfer();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(TEST_LOGIN, result.getFirst().getLogin());

		verify(restClient).getForObject(anyString(), any(ParameterizedTypeReference.class));
	}

	@Test
	void getAccountsForTransfer_RestClientException_ThrowsException() {
		when(restClient.getForObject(anyString(), any(ParameterizedTypeReference.class)))
				.thenThrow(new RestClientException("Service unavailable"));

		assertThrows(RestClientException.class, () -> frontAccountService.getAccountsForTransfer());

		verify(restClient).getForObject(anyString(), any(ParameterizedTypeReference.class));
	}

	@Test
	void modifyAccount_Success() {
		LocalDate birthdate = LocalDate.of(1990, 1, 1);
		when(securityUtils.getCurrentUsername()).thenReturn(TEST_LOGIN);
		//doNothing().when(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));

		assertDoesNotThrow(() -> frontAccountService.modifyAccount(TEST_NAME, birthdate));

		verify(securityUtils).getCurrentUsername();
		verify(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));
	}

	@Test
	void modifyAccount_UserUnder18_ThrowsValidationException() {
		LocalDate underageBirthdate = LocalDate.now().minusYears(17);
		when(securityUtils.getCurrentUsername()).thenReturn(TEST_LOGIN);

		ValidationException exception = assertThrows(ValidationException.class,
				() -> frontAccountService.modifyAccount(TEST_NAME, underageBirthdate)
		);

		assertTrue(exception.getMessage().contains("не может быть младше 18 лет"));

		verify(securityUtils).getCurrentUsername();
		verify(restClient, never()).postForObject(anyString(), any(), any());
	}

	@Test
	void modifyAccount_Exactly18YearsOld_Success() {
		LocalDate birthdate = LocalDate.now().minusYears(18);
		when(securityUtils.getCurrentUsername()).thenReturn(TEST_LOGIN);
		//doNothing().when(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));

		assertDoesNotThrow(() -> frontAccountService.modifyAccount(TEST_NAME, birthdate));

		verify(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));
	}

	@Test
	void modifyAccount_InvalidNameFormat_StillProcesses() {
		LocalDate birthdate = LocalDate.of(1990, 1, 1);
		String invalidName = "SingleName";
		when(securityUtils.getCurrentUsername()).thenReturn(TEST_LOGIN);
		//doNothing().when(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));

		ValidationException exception = assertThrows(ValidationException.class,
				() -> frontAccountService.modifyAccount(invalidName, birthdate)
		);
		assertTrue(exception.getMessage().contains("должно быть имя и фамилия"));

		verify(restClient, never()).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));
	}

	@Test
	void modifyAccount_RestClientException_ThrowsException() {
		LocalDate birthdate = LocalDate.of(1990, 1, 1);
		when(securityUtils.getCurrentUsername()).thenReturn(TEST_LOGIN);
		doThrow(new RestClientException("Service error"))
				.when(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));

		assertThrows(RestClientException.class,
				() -> frontAccountService.modifyAccount(TEST_NAME, birthdate)
		);

		verify(restClient).postForObject(anyString(), any(ClientModifyDto.class), eq(ClientDto.class));
	}
}
