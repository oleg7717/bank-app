package ru.goncharenko.bankclient.common.mapper;

import org.springframework.stereotype.Component;
import ru.goncharenko.bankclient.common.model.AntifraudDto;
import ru.goncharenko.bankclient.common.model.cashoperation.DepositOrWithdrawDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsFrontDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsFrontDto;

@Component
public class AntifraudMapper {
	public <R> AntifraudDto mapToAntifraudDto(R dto) {
		return switch(dto) {
			case TransferBetweenAccountsFrontDto transferDto -> AntifraudDto.builder()
						.fromAccount(transferDto.getFromAccount())
						.toAccount(transferDto.getToAccount())
						.betweenOwnAccounts(false)
						.amount(transferDto.getAmount())
						.build();
			case TransferBetweenOwnAccountsFrontDto transferDto -> AntifraudDto.builder()
						.fromAccount(transferDto.getLogin())
						.betweenOwnAccounts(true)
						.amount(transferDto.getAmount())
						.build();
			case DepositOrWithdrawDto depositOrWithdrawDto -> AntifraudDto.builder()
						.fromAccount(depositOrWithdrawDto.getLogin())
						.betweenOwnAccounts(true)
						.amount(depositOrWithdrawDto.getAmount())
						.build();
			default -> throw new IllegalStateException("Unexpected value: " + dto);
		};
	}
}
