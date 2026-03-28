package ru.goncharenko.bankfront.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.goncharenko.bankfront.model.AccountDto;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final RestClient restClient;

	public AccountDto getAccount() {
		AccountDto account = restClient.get()
				.uri("/api/account")
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
					System.err.println("Custom 4xx handler: " + response.getStatusCode());
				})
				.body(AccountDto.class);
		return null;
	}
}
