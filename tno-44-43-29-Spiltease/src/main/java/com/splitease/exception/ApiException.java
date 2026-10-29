package com.splitease.exception;

/**
 * Thrown by the service layer when a request cannot be completed.
 * Carries the HTTP status code that the REST layer should return.
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() { return status; }

    public static ApiException badRequest(String message) { return new ApiException(400, message); }

    public static ApiException notFound(String message) { return new ApiException(404, message); }

    public static ApiException conflict(String message) { return new ApiException(409, message); }

    public static ApiException serverError(String message, Throwable cause) {
        return new ApiException(500, message, cause);
    }
}
