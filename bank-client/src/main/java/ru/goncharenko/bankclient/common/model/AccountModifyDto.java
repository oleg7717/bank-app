package ru.goncharenko.bankclient.common.model;

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
public class AccountModifyDto {
	private String firstname;
	private String surname;
	private LocalDate birthdate;
}
