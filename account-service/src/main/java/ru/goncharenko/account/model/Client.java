package ru.goncharenko.account.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.goncharenko.account.model.enums.ClientStatus;

import java.time.LocalDate;

@Table(name = "clients")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
	@Id
	private Long id;

	@Column("login")
	private String login;

	@Column("firstname")
	private String firstname;

	@Column("surname")
	private String surname;

	@Column("status")
	private ClientStatus status;

	@Column("birthdate")
	private LocalDate birthdate;

	@Column("balance")
	private Double balance;
}
