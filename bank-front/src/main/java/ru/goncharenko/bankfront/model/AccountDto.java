package ru.goncharenko.bankfront.model;

import java.time.LocalDate;

public record AccountDto(
		String login,
		String name,
		LocalDate birthdate
) {
}
