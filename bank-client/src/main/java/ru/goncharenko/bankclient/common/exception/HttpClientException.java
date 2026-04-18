package ru.goncharenko.bankclient.common.exception;

import lombok.Getter;

@Getter
public class HttpClientException extends RuntimeException {
	private final Integer httpStatusCode;

	public HttpClientException(String message, Integer httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}
}
