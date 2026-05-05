package ru.goncharenko.exchange.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.bankclient.common.model.CurrencyRateDto;
import ru.goncharenko.exchange.model.CurrencyRate;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CurrencyRateMapper {
	CurrencyRate dtoToEntity(CurrencyRateDto dto);
}
