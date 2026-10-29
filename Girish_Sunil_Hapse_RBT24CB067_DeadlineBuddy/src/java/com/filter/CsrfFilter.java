package com.filter;

import com.util.Web;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Rejects any POST that does not carry this session's CSRF token.
 *
 * Every state change in the application is a POST, so this one filter covers
 * all of them - creating, editing, deleting, completing and reopening, plus
 * sign-in, registration and sign-out. GET handlers change nothing, so they are
 * left alone.
 *
 * A missing or wrong token is forwarded to the error page with a real 403.
 * It is a forward and not a redirect on purpose: setStatus followed by
 * sendRedirect would leave the browser with a 302 and no 403 at all.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final String PARAM = "csrf";

    @Override
    public void init(FilterConfig config) throws ServletException {
        // nothing to configure
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (tokenMatches(req)) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "Request rejected");
        req.setAttribute("errorMessage",
                "This form was not submitted from DeadlineBuddy, or your session expired "
                + "while the page was open. Please go back, reload the page and try again.");
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, res);
    }

    private boolean tokenMatches(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(Web.SESSION_CSRF);
        if (expected == null) {
            return false;
        }
        String submitted = req.getParameter(PARAM);
        if (submitted == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
