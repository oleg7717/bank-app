package ru.goncharenko.bankclient.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageApiResponse implements ApiResponse {
	private String message;
	private int statusCode;

	public static MessageApiResponse error(String message, int statusCode) {
		return MessageApiResponse.builder()
				.message(message)
				.statusCode(statusCode)
				.build();
	}
}
