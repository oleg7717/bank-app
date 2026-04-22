package ru.goncharenko.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.account.model.Account;
import ru.goncharenko.bankclient.common.model.account.AccountDto;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountMapper {
	AccountDto mapEntityToDto(Account account);
}
