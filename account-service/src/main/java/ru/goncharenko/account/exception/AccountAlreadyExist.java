package ru.goncharenko.account.exception;

public class AccountAlreadyExist extends RuntimeException {
	public AccountAlreadyExist(String message) {
		super(message);
	}
}
