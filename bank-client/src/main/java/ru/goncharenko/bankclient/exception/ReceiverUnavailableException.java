package ru.goncharenko.bankclient.exception;

import lombok.Getter;

@Getter
public class ReceiverUnavailableException extends RuntimeException {
	private final Integer httpStatusCode;

	public ReceiverUnavailableException(String message, Integer httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}
}
