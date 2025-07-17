package it.uniroma3.cashlytics.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

	public UnauthorizedAccessException(String message) {
		super(message);
	}

	public UnauthorizedAccessException(String resource, Long resourceId) {
		super("Unauthorized access to " + resource + " with ID " + resourceId);
	}

}