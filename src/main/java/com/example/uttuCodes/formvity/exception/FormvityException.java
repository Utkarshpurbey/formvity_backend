package com.example.uttuCodes.formvity.exception;

import org.springframework.http.HttpStatus;

/**
 * Application exception mapped to {@link com.example.uttuCodes.formvity.dto.response.ErrorResponse}:
 * <ul>
 *   <li>{@code error} — HTTP reason phrase for {@link #getStatus()} (short, stable category)</li>
 *   <li>{@code errorMessage} — {@link #getMessage()} (detail for clients)</li>
 * </ul>
 */
public class FormvityException extends RuntimeException {

    private final HttpStatus status;

    public FormvityException(HttpStatus status, String message) {
        super(message != null ? message : status.getReasonPhrase());
        this.status = status;
    }

    public FormvityException(HttpStatus status, String message, Throwable cause) {
        super(message != null ? message : status.getReasonPhrase(), cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Short label for the API {@code error} field — same as {@link HttpStatus#getReasonPhrase()}
     * for {@link #getStatus()}, so it is not a second copy of the detail text.
     */
    public String getError() {
        return status.getReasonPhrase();
    }

    public static FormvityException badRequest(String message) {
        return new FormvityException(HttpStatus.BAD_REQUEST, message);
    }

    public static FormvityException notFound(String message) {
        return new FormvityException(HttpStatus.NOT_FOUND, message);
    }

    public static FormvityException conflict(String message) {
        return new FormvityException(HttpStatus.CONFLICT, message);
    }

    public static FormvityException forbidden(String message) {
        return new FormvityException(HttpStatus.FORBIDDEN, message);
    }

    public static FormvityException unauthorized(String message) {
        return new FormvityException(HttpStatus.UNAUTHORIZED, message);
    }

    public static FormvityException internalServerError(String message) {
        return new FormvityException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static FormvityException serviceUnavailable(String message, Throwable cause) {
        return new FormvityException(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
