package ru.goncharenko.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.service.AccountService;
import ru.goncharenko.bankclient.common.model.ClientDto;
import ru.goncharenko.bankclient.common.model.ClientListDto;
import ru.goncharenko.bankclient.common.model.ClientModifyDto;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ACCOUNT_BASE_URL;
import static ru.goncharenko.bankclient.common.endpoint.Endpoints.ACCOUNT_LIST;

@RestController
@RequestMapping(ACCOUNT_BASE_URL)
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@GetMapping
	public Mono<ResponseEntity<ClientDto>> getClientData() {
		return accountService.getClientData();
	}

	@RequestMapping(method = {POST, PUT}, path = "/{login}")
	public Mono<ResponseEntity<ClientDto>> modifyPersonalData(@RequestBody Mono<ClientModifyDto> clientDto,
	                                                          @PathVariable("login") String login) {
		return accountService.modifyPersonalData(clientDto, login);
	}

	@GetMapping(ACCOUNT_LIST)
	public Flux<ClientListDto> getClientsListForTransfer() {
		return accountService.getClientsListForTransfer();
	}
}
