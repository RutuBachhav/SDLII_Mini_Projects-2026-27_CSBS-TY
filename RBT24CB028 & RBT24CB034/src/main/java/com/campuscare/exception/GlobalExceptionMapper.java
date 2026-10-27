package com.campuscare.exception;

import com.campuscare.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof AppException) {
            AppException appEx = (AppException) exception;
            return Response.status(appEx.getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(appEx.getMessage()))
                    .build();
        }

        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            String violations = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error("Validation error: " + violations))
                    .build();
        }

        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            return Response.status(wae.getResponse().getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(wae.getMessage()))
                    .build();
        }

        exception.printStackTrace();
        String errorMsg = exception.getMessage() != null ? exception.getMessage() : "Internal server error occurred";
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(errorMsg))
                .build();
    }
}
