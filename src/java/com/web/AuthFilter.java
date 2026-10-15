package com.web;

import com.util.Web;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
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
 * Everything behind these paths needs a signed-in user. The check is here, in
 * one place, rather than repeated at the top of every servlet.
 */
@WebFilter(urlPatterns = {"/dashboard", "/report", "/items/edit", "/items/close", "/items/reopen",
                          "/claims/*", "/notifications", "/notifications/read", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (Web.currentUser(request) != null) {
            chain.doFilter(req, res);
            return;
        }

        if ("GET".equals(request.getMethod())) {
            // Remember where they were going, so sign-in lands them there.
            String target = request.getServletPath()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            response.sendRedirect(request.getContextPath() + "/login?next="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
        } else {
            Web.flash(request, "error", "Your session expired. Sign in and try again.");
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
