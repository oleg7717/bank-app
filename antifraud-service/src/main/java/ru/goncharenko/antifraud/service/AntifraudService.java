package ru.goncharenko.antifraud.service;

import org.springframework.stereotype.Service;
import ru.goncharenko.bankclient.common.model.AntifraudDto;
import ru.goncharenko.bankclient.common.response.AntiFraudResponse;

import java.util.List;
import java.util.Random;

@Service
public class AntifraudService {
	private final List<String> blockingList = List.of("h.jackman");

	public AntiFraudResponse checkOperation(AntifraudDto antifraudRequest) {
		int rand = new Random().nextInt(100);
		if (antifraudRequest.isBetweenAccountTransfer()) {
			if (rand < 15) {
				return AntiFraudResponse.blockedOperation("Подозрительная операция перевода между своими счетами");
			}
		} else {
			if (rand < 25) {
				return AntiFraudResponse.blockedOperation(
						String.format(
								"Подозрительная операция при переводе на счёт пользователя: %s",
								antifraudRequest.getToAccount())
				);
			} else if (blockingList.contains(antifraudRequest.getToAccount()) && rand < 80) {
				return AntiFraudResponse.blockedOperation(
						String.format(
								"Частое пополнение счёта пользователя: %s",
								antifraudRequest.getToAccount())
				);
			}
		}

		return AntiFraudResponse.allowedOperation(
				String.format(
						"Операция пользователя: %s одобрена",
						antifraudRequest.getFromAccount())
		);
	}
}
