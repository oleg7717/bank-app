package ru.goncharenko.bankclient.common.model.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientListDto {
	private String login;
	private String name;
	private String currency;
}
