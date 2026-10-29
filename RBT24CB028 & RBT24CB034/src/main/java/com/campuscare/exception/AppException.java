package com.campuscare.exception;

import jakarta.ws.rs.core.Response;

public class AppException extends RuntimeException {

    private final Response.Status status;

    public AppException(String message) {
        super(message);
        this.status = Response.Status.BAD_REQUEST;
    }

    public AppException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
