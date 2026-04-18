package ru.goncharenko.bankclient.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goncharenko.bankclient.common.enums.AntifraudCheckStatus;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AntiFraudResponse {
	private AntifraudCheckStatus status;
	private String message;

	public static AntiFraudResponse blockedOperation(String message) {
		return AntiFraudResponse.builder().status(AntifraudCheckStatus.BLOCKED)
				.message(message)
				.build();
	}

	public static AntiFraudResponse allowedOperation(String message) {
		return AntiFraudResponse.builder().status(AntifraudCheckStatus.BLOCKED)
				.message(message)
				.build();
	}
}
