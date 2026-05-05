package ru.goncharenko.bankclient.common.model.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
	private String login;
	private String firstname;
	private String surname;
	private LocalDate birthdate;
	private Double balance;
}
