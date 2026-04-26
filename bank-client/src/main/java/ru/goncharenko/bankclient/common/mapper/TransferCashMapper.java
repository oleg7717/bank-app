package ru.goncharenko.bankclient.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenAccountsFrontDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsDto;
import ru.goncharenko.bankclient.common.model.cashoperation.TransferBetweenOwnAccountsFrontDto;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransferCashMapper {
	@Mapping(source = "toCurrency", target = "currency")
	TransferBetweenAccountsDto mapFromFrontDtoToAccount(TransferBetweenAccountsFrontDto dto);

	@Mapping(source = "toCurrency", target = "currency")
	TransferBetweenOwnAccountsDto mapFromFrontDtoToAccount(TransferBetweenOwnAccountsFrontDto dto);
}
