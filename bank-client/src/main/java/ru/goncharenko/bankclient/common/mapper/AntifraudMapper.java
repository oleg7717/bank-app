package ru.goncharenko.bankclient.common.mapper;

import org.springframework.stereotype.Component;
import ru.goncharenko.bankclient.common.model.AntifraudDto;
import ru.goncharenko.bankclient.common.model.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.common.model.TransferCashDto;

@Component
public class AntifraudMapper {
	public <R> AntifraudDto mapToAntifraudDto(R dto) {
		switch(dto) {
			case TransferCashDto transferDto -> {
				return AntifraudDto.builder()
						.fromAccount(transferDto.getFromAccount())
						.toAccount(transferDto.getToAccount())
						.betweenOwnAccounts(false)
						.amount(transferDto.getAmount())
						.build();

			}
			case DepositOrWithdrawDto depositOrWithdrawDto -> {
				return AntifraudDto.builder()
						.fromAccount(depositOrWithdrawDto.getLogin())
						.betweenOwnAccounts(true)
						.amount(depositOrWithdrawDto.getAmount())
						.build();
			}
			default -> throw new IllegalStateException("Unexpected value: " + dto);
		}
	}
}
