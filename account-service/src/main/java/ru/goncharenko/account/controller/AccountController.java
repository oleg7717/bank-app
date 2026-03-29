package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.service.AccountService;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountModifyDto;

import static ru.goncharenko.bankclient.endpoint.Endpoints.*;

@RestController
@RequestMapping(ACCOUNT_BASE_URL)
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@GetMapping
	public Mono<ResponseEntity<AccountDto>> getAccount() {
		return accountService.getAccount();
	}

	@PutMapping(path = "/{login}")
	public Mono<ResponseEntity<AccountDto>> modifyAccount(@RequestBody Mono<AccountModifyDto> accountDto,
	                                                    @PathVariable("login") String login) {
		return accountService.modifyAccount(accountDto, login);
	}

	@GetMapping(TRANSFER_LIST)
	public Flux<AccountDto> getTransferAccounts() {
		return accountService.getAllAccounts();
	}
}
