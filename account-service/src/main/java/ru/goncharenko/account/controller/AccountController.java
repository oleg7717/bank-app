package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.dto.AccountDto;
import ru.goncharenko.account.model.dto.AccountModifyDto;
import ru.goncharenko.account.service.AccountService;

import static ru.goncharenko.account.controller.endpoint.Endpoints.*;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
public class AccountController {
	private final AccountService service;

	@GetMapping
	public Mono<ResponseEntity<AccountDto>> getAccount() {
		return service.getAccount();
	}

	@PutMapping
	public Mono<ResponseEntity<AccountDto>> editAccount(Mono<AccountModifyDto> accountDto) {
		return service.modifyAccount(accountDto);
	}

	@GetMapping(TRANSFER_LIST)
	public Flux<AccountDto> getTransferAccounts() {
		return service.getAllAccounts();
	}
}
