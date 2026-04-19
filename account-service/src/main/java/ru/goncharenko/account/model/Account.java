package ru.goncharenko.account.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.goncharenko.account.model.enums.Currency;

@Table(name = "accounts")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
	@Id
	private Long id;

	@Column("client_id")
	private String clientId;

	@Column("status")
	private String status;

	@Column("main")
	private Boolean main;

	@Column("balance")
	private Double balance;

	@Column("currency")
	private Currency currency;
}
