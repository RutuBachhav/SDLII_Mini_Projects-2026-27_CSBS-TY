package com.hospital.web;

import com.hospital.module.Role;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Guards /patient/* and /doctor/*.
 * Defence in depth: every servlet re-checks ownership on the data itself,
 * so this filter is the outer gate, not the only check.
 */
@WebFilter(urlPatterns = {"/patient/*", "/doctor/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();

        Long uid = SessionUtil.userId(req);
        Role role = SessionUtil.role(req);
        if (uid == null || role == null) {
            res.sendRedirect(req.getContextPath() + "/login?next=" + path);
            return;
        }
        boolean patientArea = path.startsWith("/patient/");
        boolean doctorArea = path.startsWith("/doctor/");
        if ((patientArea && role != Role.PATIENT) || (doctorArea && role != Role.DOCTOR)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorTitle", "Not allowed");
            req.setAttribute("errorMessage", "Your account does not have access to this section.");
            req.setAttribute("pageTitle", "Not allowed");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
