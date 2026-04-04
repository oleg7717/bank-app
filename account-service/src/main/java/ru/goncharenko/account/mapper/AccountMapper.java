package ru.goncharenko.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.account.model.entity.Account;
import ru.goncharenko.bankclient.model.AccountDto;
import ru.goncharenko.bankclient.model.AccountListDto;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountMapper {
	AccountDto mapToDto(Account account);

	@Mapping(source = ".", target = "name", qualifiedByName = "concatName")
	AccountListDto mapToList(Account account);

	@Named("concatName")
	default String concatName(Account account) {
		if (account == null) return null;
		return account.getSurname() + " " + account.getFirstname();
	}
}
