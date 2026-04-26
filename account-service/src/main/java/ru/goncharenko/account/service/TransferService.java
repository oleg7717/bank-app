package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.model.enums.ClientStatus;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.account.utils.CommonLogic;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferCashDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;

@Service
@RequiredArgsConstructor
public class TransferService {
	private final ClientRepository clientRepository;
	private final AccountService accountService;

	@PreAuthorize("hasRole('transfer_cash')")
	@Transactional
	public Mono<ResponseEntity<SuccessResponse>> transferCash(Mono<TransferCashDto> transferCashDto) {
		return transferCashDto.flatMap(dto -> clientRepository
				.findByLoginAndStatus(dto.getFromAccount(), ClientStatus.ACTIVE)
				.switchIfEmpty(CommonLogic.noAccount(dto.getFromAccount()))
				.flatMap(client -> accountService.getAccountByClientIdAndCurrency(client.getId(), dto.getCurrency())
					.flatMap(account -> {
						Double balance = account.getBalance();
						Double amount = dto.getAmount();
						if (balance < amount) {
							return Mono.error(new ResponseStatusException(
									HttpStatus.CONFLICT,
									"Недостаточно средств на балансе"
							));
						}

						return Mono.zip(
								accountService.transferCashFrom(dto.getAmount(), dto.getFromAccount(), dto.getCurrency()),
								accountService.transferCashTo(dto.getAmount(), dto.getToAccount(), dto.getCurrency())
						).flatMap(accountDto -> Mono.just(ResponseEntity.ok()
								.body(new SuccessResponse(HttpStatus.OK.value(), "Перевод средств выполнен")))
						);
					}))
		);
	}
}
