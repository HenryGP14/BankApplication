package com.devsu.hackerearth.backend.client.exception;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devsu.hackerearth.backend.client.model.dto.BasicResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(GenericException.class)
	public BasicResponse<Object> handleApiException(GenericException ex, HttpServletResponse response) {
		response.setStatus(ex.getStatus().value());
		return buildResponse(ex.getStatus(), ex.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public BasicResponse<Object> handleBadCredentials(BadCredentialsException ex) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public BasicResponse<Object> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return buildResponse(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Datos de la solicitud inválidos" : message);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public BasicResponse<Object> handleIllegalArgument(IllegalArgumentException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public BasicResponse<Object> handleUnexpected(Exception ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta nuevamente.");
	}

	private BasicResponse<Object> buildResponse(HttpStatus status, String message) {
		return BasicResponse.of(status, message, null);
	}
}
