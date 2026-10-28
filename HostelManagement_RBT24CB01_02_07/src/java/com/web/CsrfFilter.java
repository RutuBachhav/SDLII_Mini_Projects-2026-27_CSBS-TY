package com.web;

import com.security.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Rejects every state-changing request that does not carry the session-bound
 * CSRF token. Runs after AuthFilter.
 */
public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS =
            Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        if (path.equals("/api") || path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        if (!SAFE_METHODS.contains(request.getMethod())
                && !CsrfUtil.validate(request)) {
            SimplePage.render(request, response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Request Rejected",
                    "Request rejected",
                    "This form is missing a valid security token. "
                    + "Go back, refresh the page, and try again.",
                    "/index.jsp", "Return to Dashboard");
            return;
        }

        chain.doFilter(request, response);
    }
}
