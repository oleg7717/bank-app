package ru.goncharenko.notification.model.entity;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "notifications")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
	@Id
	private Long id;

	@Column("service")
	private String service;

	@Column("message")
	private String message;

	@Column("created")
	private LocalDateTime created;
}