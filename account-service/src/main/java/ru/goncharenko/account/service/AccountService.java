package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.mapper.AccountMapper;
import ru.goncharenko.account.model.dto.AccountDto;
import ru.goncharenko.account.model.dto.AccountModifyDto;
import ru.goncharenko.account.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository repository;
	private final AccountMapper mapper;
	String login = "o.goncharenko";

	public Mono<ResponseEntity<AccountDto>> getAccount() {
		return repository.findByLogin(login)
				.map(mapper::mapToDto)
				.flatMap(account -> Mono.just(ResponseEntity.ok()
						.body(account)))
				.switchIfEmpty(
						Mono.error(new ResponseStatusException(
								HttpStatus.NOT_FOUND, String.format("У пользователя %s нет аккаунта в банке ", login)
						))
				);
	}

	public Mono<ResponseEntity<AccountDto>> modifyAccount(Mono<AccountModifyDto> accountModifyDto) {
		return accountModifyDto.map(mapper::mapToEntity)
				.flatMap(account -> repository.save(account)
						.map(mapper::mapToDto)
						.flatMap(accountDto -> Mono.just(ResponseEntity.ok()
								.body(accountDto)))
				);
	}

	public Flux<AccountDto> getAllAccounts() {
		return repository.findAll().map(mapper::mapToDto);
	}

/*	@Transactional
	public Mono<ResponseEntity<PaymentStatus>> makePayment(Mono<Payment> payment) {
		return payment.flatMap(pay -> {
			String userName = pay.getUserName();
			Double amount = pay.getOrderAmount() == null ? 0 : pay.getOrderAmount();
			return repository.findByUserName(userName).flatMap(account -> {
						Double balance = account.getBalance();
						if (balance < amount) {
							return Mono.just(ResponseEntity.ok()
									.body(new PaymentStatus()
											.code(HttpStatus.PAYMENT_REQUIRED.value())
											.message("Недостаточно средств на счету")
											.processed(false)));
						}
						account.setBalance(BigDecimal.valueOf(balance)
								.subtract(BigDecimal.valueOf(amount))
								.setScale(2, RoundingMode.HALF_UP)
								.doubleValue()
						);
						return repository.save(account)
								.map(savedAccount -> ResponseEntity.ok()
										.body(new PaymentStatus()
												.code(HttpStatus.OK.value())
												.message("Платёж совершён")
												.processed(true)));
					})
					.switchIfEmpty(Mono.error(new ResponseStatusException(
							HttpStatus.NOT_FOUND, String.format("У пользователя %s нет счета в банке ", userName)
					)));
		});
	}*/
}
