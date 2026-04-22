package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.Client;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.bankclient.common.model.cashoperation.BalanceDto;
import ru.goncharenko.bankclient.common.model.cashoperation.DepositOrWithdrawDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CashService {
	private final ClientRepository clientRepository;

	@PreAuthorize("hasRole('cash_deposit_or_withdraw')")
	@Transactional
	public Mono<ResponseEntity<BalanceDto>> depositOrWithdraw(Mono<DepositOrWithdrawDto> depositOrWithdrawDto) {
		return depositOrWithdrawDto.flatMap(dto -> clientRepository
				.findByLogin(dto.getLogin())
				.flatMap(account -> {
					String login = dto.getLogin();
					Double balance = account.getBalance();
					Double amount = dto.getAmount();
					if (balance < amount) {
						return Mono.error(new ResponseStatusException(
								HttpStatus.CONFLICT,
								"Недостаточно средств на балансе"
						));
					}
					switch (dto.getAction()) {
						case GET -> decrease(account, amount);
						case PUT -> increase(account, amount);
					}

					Double changedBalance = account.getBalance();
					return clientRepository.updateBalance(changedBalance, login)
							.flatMap(accountDto -> Mono.just(ResponseEntity.ok()
									.body(new BalanceDto(changedBalance)))
							);
				})
		);
	}

	private void decrease(Client client, Double amount) {
		client.setBalance(BigDecimal.valueOf(client.getBalance())
				.subtract(BigDecimal.valueOf(amount))
				.setScale(2, RoundingMode.HALF_UP)
				.doubleValue()
		);
	}

	private void increase(Client client, Double amount) {
		client.setBalance(BigDecimal.valueOf(client.getBalance())
				.add(BigDecimal.valueOf(amount))
				.setScale(2, RoundingMode.HALF_UP)
				.doubleValue());
	}
}
