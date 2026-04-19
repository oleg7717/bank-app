package ru.goncharenko.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.account.model.Client;
import ru.goncharenko.bankclient.common.model.ClientDto;
import ru.goncharenko.bankclient.common.model.ClientListDto;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {
	ClientDto mapToDto(Client client);

	@Mapping(source = ".", target = "name", qualifiedByName = "concatName")
	ClientListDto mapToList(Client client);

	@Named("concatName")
	default String concatName(Client client) {
		if (client == null) return null;
		return client.getSurname() + " " + client.getFirstname();
	}
}
