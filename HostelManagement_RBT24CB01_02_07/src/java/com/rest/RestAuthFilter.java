package com.rest;

import com.security.SessionAuth;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Session-based authentication for the read-only REST API. API clients get a
 * JSON 401 instead of an HTML login redirect.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RestAuthFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        HttpSession session = servletRequest.getSession(false);
        if (!SessionAuth.isAuthenticated(session)) {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Session")
                    .entity("{\"error\":\"Unauthorized\","
                            + "\"message\":\"Authentication required\"}")
                    .build());
        }
    }
}
