package com.pms.filter;

import com.pms.util.AuthUtil;
import com.pms.util.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

/**
 * Role-based authorization. Public routes pass through; /student/* requires a
 * STUDENT session, /admin/* (except /admin-setup, gated by the servlet itself)
 * requires ADMIN. Also ensures every session carries a CSRF token and sets
 * baseline security headers.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC = Set.of(
            "/", "/index.jsp",
            "/student-login.jsp", "/admin-login.jsp", "/student-registration.jsp",
            "/student-login", "/admin-login", "/student-register",
            "/admin-setup", "/test-db", "/api/stats", "/error.jsp");

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }

        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || PUBLIC.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        // Direct access to the setup JSP must go through the servlet so the
        // one-time gate (token + zero-admins check) always applies.
        if (path.equals("/admin-setup.jsp")) {
            request.getRequestDispatcher("/admin-setup").forward(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (path.equals("/logout")) {
            if (!AuthUtil.isLoggedIn(session)) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        if (path.startsWith("/student/")) {
            if (!AuthUtil.isStudent(session)) {
                redirectToLogin(request, response,
                        AuthUtil.isAdmin(session) ? null : "/student-login.jsp", path);
                return;
            }
            CsrfUtil.getToken(session);
            if (path.endsWith(".jsp")) {
                // Force controller servlets (which load data) instead of raw JSPs.
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        if (path.startsWith("/admin/")) {
            if (!AuthUtil.isAdmin(session)) {
                redirectToLogin(request, response,
                        AuthUtil.isStudent(session) ? null : "/admin-login.jsp", path);
                return;
            }
            CsrfUtil.getToken(session);
            if (path.endsWith(".jsp") && !path.equals("/admin-setup.jsp")) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response,
            String loginPage, String attempted) throws IOException {
        if (loginPage == null) {
            // Wrong role (e.g. student hitting /admin/*): 403, no redirect loop.
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return;
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("flashError", "Please log in to continue.");
        if (!attempted.equals("/student/dashboard") && !attempted.equals("/admin/dashboard")) {
            session.setAttribute("postLoginNext", attempted);
        }
        response.sendRedirect(request.getContextPath() + loginPage);
    }

    @Override
    public void destroy() {
    }
}
