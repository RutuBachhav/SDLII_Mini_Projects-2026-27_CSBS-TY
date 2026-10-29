package com.labloop.rest;

import com.labloop.util.LabLoopException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Turns business-rule exceptions into proper HTTP status codes + JSON, for every REST resource. */
@Provider
public class LabLoopExceptionMapper implements ExceptionMapper<LabLoopException> {

    @Override
    public Response toResponse(LabLoopException e) {
        Response.Status status;
        switch (e.getKind()) {
            case NOT_FOUND: status = Response.Status.NOT_FOUND; break;        // 404
            case CONFLICT: status = Response.Status.CONFLICT; break;          // 409
            case FORBIDDEN: status = Response.Status.FORBIDDEN; break;        // 403
            default: status = Response.Status.BAD_REQUEST;                    // 400
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(status.getStatusCode(), e.getKind().name(), e.getMessages()))
                .build();
    }
}
