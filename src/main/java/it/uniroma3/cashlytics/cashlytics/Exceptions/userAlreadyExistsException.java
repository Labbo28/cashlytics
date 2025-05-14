package it.uniroma3.cashlytics.cashlytics.Exceptions;

public class userAlreadyExistsException extends RuntimeException {

    public userAlreadyExistsException(String message) {
        super(message);
    }

    public userAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public userAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public userAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
