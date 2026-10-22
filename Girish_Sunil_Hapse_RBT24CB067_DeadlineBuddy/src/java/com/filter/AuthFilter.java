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
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Guards everything that needs a signed-in student.
 *
 * It covers the pages and all the task and subject operations, so there is no
 * servlet that has to remember to check the session for itself. The views are
 * under WEB-INF and so are not reachable by URL at all.
 *
 * An unauthenticated request is sent to the sign-in page with the address it
 * was trying to reach, so signing in continues where the student left off.
 * {@code next} is only ever used as a path inside this application - see
 * AuthServlet.safeNext - so it cannot be turned into an open redirect.
 */
@WebFilter(filterName = "AuthFilter",
           urlPatterns = {"/dashboard", "/tasks", "/tasks/*", "/subjects", "/subjects/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        // nothing to configure
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (Web.currentUserId(req) != null) {
            // Never let an authenticated page sit in a shared cache.
            res.setHeader("Cache-Control", "no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            chain.doFilter(request, response);
            return;
        }

        String target = req.getRequestURI();
        if (req.getQueryString() != null) {
            target = target + "?" + req.getQueryString();
        }
        String next = URLEncoder.encode(target, StandardCharsets.UTF_8);
        res.sendRedirect(req.getContextPath() + "/login?next=" + next);
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
