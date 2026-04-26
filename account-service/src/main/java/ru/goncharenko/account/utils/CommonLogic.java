package ru.goncharenko.account.utils;

import reactor.core.publisher.Mono;
import ru.goncharenko.bankclient.common.exception.NotFoundException;

public class CommonLogic {
	public static <T> Mono<T> noAccount(String username) {
		return Mono.error(
				new NotFoundException(String.
						format("У пользователя %s нет аккаунта в банке ", username))
		);
	}
}
