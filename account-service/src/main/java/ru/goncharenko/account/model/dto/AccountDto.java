package ru.goncharenko.account.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
	private String login;
	private String firstname;
	private String surname;
	private LocalDate birthdate;
	private Double balance;
}
