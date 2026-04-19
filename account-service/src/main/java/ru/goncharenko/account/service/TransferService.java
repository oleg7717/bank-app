package ru.goncharenko.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.goncharenko.account.repository.ClientRepository;
import ru.goncharenko.bankclient.common.model.TransferCashDto;
import ru.goncharenko.bankclient.common.response.SuccessResponse;

@Service
@RequiredArgsConstructor
public class TransferService {
	private final ClientRepository clientRepository;

	@PreAuthorize("hasRole('transfer_cash')")
	@Transactional
	public Mono<ResponseEntity<SuccessResponse>> transferCash(Mono<TransferCashDto> transferCashDto) {
		return transferCashDto.flatMap(dto -> clientRepository
				.findByLogin(dto.getFromAccount())
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
							clientRepository.transferCashFrom(dto.getAmount(), dto.getFromAccount()),
							clientRepository.transferCashTo(dto.getAmount(), dto.getToAccount())
					).flatMap(accountDto -> Mono.just(ResponseEntity.ok()
							.body(new SuccessResponse(HttpStatus.OK.value(), "Перевод средств выполнен")))
					);
				})
		);
	}
}
