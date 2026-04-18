package ru.goncharenko.bankclient.web.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.goncharenko.bankclient.common.exception.NotFoundException;
import ru.goncharenko.bankclient.common.exception.ValidationException;
import ru.goncharenko.bankclient.common.response.MessageApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public MessageApiResponse handleResourceNotFoundException(NotFoundException ex) {
		return MessageApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public MessageApiResponse handleValidationException(ValidationException ex) {
		return MessageApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public MessageApiResponse handleGenericException(Exception ex) {
		return MessageApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public MessageApiResponse handleAccessDeniedException(AccessDeniedException ex) {
		log.warn("Access denied: {}", ex.getMessage());
		return MessageApiResponse.error(
				"Access denied: " + ex.getMessage(),
				HttpStatus.FORBIDDEN.value()
		);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public MessageApiResponse handleAuthenticationException(AuthenticationException ex) {
		log.warn("Authentication failed: {}", ex.getMessage());
		return MessageApiResponse.error(
				"Authentication failed: " + ex.getMessage(),
				HttpStatus.UNAUTHORIZED.value()
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public MessageApiResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
		StringBuilder textErrors = new StringBuilder();
		ex.getBindingResult().getAllErrors()
				.forEach(error -> textErrors.append(error.getDefaultMessage()).append(". "));

		return MessageApiResponse
				.error(String.join(". ", textErrors.toString().trim()), HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
	@ResponseBody
	public MessageApiResponse handleConstraintViolationException(ConstraintViolationException ex) {
		return MessageApiResponse.error(ex.getMessage(), HttpStatus.UNPROCESSABLE_CONTENT.value());
	}
}
