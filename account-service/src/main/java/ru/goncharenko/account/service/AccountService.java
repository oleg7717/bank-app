package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.exception.AccountAlreadyExist;
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.mapper.ClientMapper;
import ru.goncharenko.account.model.entity.Account;
import ru.goncharenko.account.model.enums.AccountStatus;
import ru.goncharenko.account.model.enums.ClientStatus;
import ru.goncharenko.account.repository.AccountRepository;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.account.utils.CommonLogic;
import ru.goncharenko.bankclient.common.model.account.AccountDto;
import ru.goncharenko.bankclient.common.model.account.CreateAccountDto;
import ru.goncharenko.bankclient.common.model.client.ClientListDto;
import ru.goncharenko.bankclient.reactive.utils.SecurityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final ClientRepository clientRepository;
	private final AccountRepository accountRepository;

	private final ClientMapper clientMapper;
	private final AccountMapper accountMapper;

	private final TransactionalOperator transactionalOperator;
	private final SecurityUtils securityUtils;

	public Mono<ResponseEntity<AccountDto>> createAccount(Mono<CreateAccountDto> accountDto) {
		return securityUtils.getCurrentUsername()
				.flatMap(username -> clientRepository.findByLoginAndStatus(username, ClientStatus.ACTIVE)
						.switchIfEmpty(CommonLogic.noAccount(username))
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
				)
				.as(transactionalOperator::transactional);
	}

	public Flux<ClientListDto> getClientAccountListForTransfer() {
		return clientRepository.findClientsByStatus(ClientStatus.ACTIVE).map(clientMapper::mapToList);
	}

	public Mono<Account> getAccountByClientIdAndCurrency(Long clientId, String currency) {
		return accountRepository.getAccountByClientIdAndCurrency(clientId, currency);
	}

	public Mono<Integer> updateBalance(Double changedBalance, String login, String currency) {
		return accountRepository.updateBalance(changedBalance, login, currency);
	}

	public Mono<Integer> transferCashFrom(Double changedBalance, String login, String currency) {
		return accountRepository.transferCashFrom(changedBalance, login, currency);
	}

	public Mono<Integer> transferCashTo(Double changedBalance, String login, String currency) {
		return accountRepository.transferCashTo(changedBalance, login, currency);
	}

/*
	Блок курсов валют
        Первый столбец таблицы — валюта.
        Второй столбец — покупка.
        Третий столбец — продажа.
*/
}
