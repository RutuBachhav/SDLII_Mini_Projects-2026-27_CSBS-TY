package com.splitease.rest;

import com.splitease.dto.ErrorMessage;
import com.splitease.exception.ApiException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Turns every ApiException into a clean JSON error with the right HTTP status. */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class.getName());

    @Override
    public Response toResponse(ApiException ex) {
        if (ex.getStatus() >= 500) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return Response.status(ex.getStatus())
                .entity(new ErrorMessage(ex.getStatus(), ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
