package com.web;

import com.security.CsrfUtil;
import com.security.SessionAuth;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Authentication gate for every page and servlet. REST endpoints are handled
 * by RestAuthFilter so API clients receive JSON 401 responses, not redirects.
 */
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/login", "/login.jsp",
            "/setup",
            "/access-denied.jsp",
            "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        request.setCharacterEncoding("UTF-8");

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        if (path.equals("/api") || path.startsWith("/api/")
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        boolean publicPath = PUBLIC_PATHS.contains(path);
        HttpSession session = request.getSession(false);
        boolean authenticated = SessionAuth.isAuthenticated(session);

        if (!authenticated && !publicPath) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        if (authenticated && ("/login".equals(path)
                || "/login.jsp".equals(path)
                || "/setup".equals(path))) {
            response.sendRedirect(contextPath + "/index.jsp");
            return;
        }

        if (session == null) {
            session = request.getSession(true);
        }
        CsrfUtil.ensureToken(session);
        Layout.begin(request);
        try {
            response.setHeader("Cache-Control",
                    "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            chain.doFilter(request, response);
        } finally {
            Layout.end();
        }
    }
}
