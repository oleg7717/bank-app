package ru.goncharenko.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.goncharenko.notification.model.entity.Notification;
import ru.goncharenko.bankclient.common.model.NotificationDto;

@Mapper(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		componentModel = MappingConstants.ComponentModel.SPRING,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {
	Notification dtoToEntity(NotificationDto dto);
}
