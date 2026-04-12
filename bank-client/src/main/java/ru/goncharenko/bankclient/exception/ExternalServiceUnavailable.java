package ru.goncharenko.bankclient.exception;

import lombok.Getter;

@Getter
public class ExternalServiceUnavailable extends RuntimeException {
	private final Integer httpStatusCode;

	public ExternalServiceUnavailable(String message, Integer httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}
}
