package com.labloop.filter;

import com.labloop.cdi.UserSession;
import com.labloop.service.LabLoopSettingsBean;
import com.labloop.service.PracticalKitBean;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Runs before EVERY request. Sends guests to the login page and makes common data
 * (current user, institution name, kit size) available to every JSP.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Inject
    private UserSession me;

    @Inject
    private PracticalKitBean kit;

    @EJB
    private LabLoopSettingsBean settings;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getRequestURI().substring(req.getContextPath().length());

        boolean isPublic = path.equals("/login") || path.startsWith("/assets/") || path.startsWith("/api/");
        if (isPublic) {
            req.setAttribute("institution", settings.getInstitutionName());
            chain.doFilter(req, resp);
            return;
        }
        if (!me.isLoggedIn()) {
            String next = path + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
            resp.sendRedirect(req.getContextPath() + "/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
            return;
        }
        req.setAttribute("me", me);
        req.setAttribute("institution", settings.getInstitutionName());
        req.setAttribute("kitSize", kit.getSize());
        resp.setHeader("Cache-Control", "no-store");   // pages show live data; don't let Back show stale copies
        chain.doFilter(req, resp);
    }
}
