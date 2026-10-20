package com.hospital.web;

import com.hospital.module.AppUser;
import com.hospital.module.HospitalDAO;
import com.hospital.module.PasswordUtil;
import com.hospital.module.Role;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Email + password login with role-based landing. Generic error avoids leaking which field failed. */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (SessionUtil.userId(req) != null) {
            res.sendRedirect(RegisterServlet.homeFor(SessionUtil.role(req), req));
            return;
        }
        req.setAttribute("pageTitle", "Sign in");
        req.setAttribute("next", req.getParameter("next"));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String next = req.getParameter("next");

        AppUser user = HospitalDAO.findUserByEmail(email == null ? "" : email);
        boolean ok = user != null
                && PasswordUtil.verify(password == null ? "" : password, user.getPasswordSalt(), user.getPasswordHash());
        if (!ok) {
            // Small constant-time courtesy: even delay is unnecessary here; generic message is what matters.
            req.setAttribute("pageTitle", "Sign in");
            req.setAttribute("loginError", "Incorrect email or password. Check both and try again.");
            req.setAttribute("email", email == null ? "" : email.trim());
            req.setAttribute("next", next);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            return;
        }
        SessionUtil.login(req, user);
        String dest = safeNext(next, req);
        if (dest == null) {
            dest = RegisterServlet.homeFor(user.getRole(), req);
        }
        res.sendRedirect(dest);
    }

    /** Only allow redirects back into our own portals to avoid open-redirect abuse. */
    private static String safeNext(String next, HttpServletRequest req) {
        if (next == null || next.isEmpty()) {
            return null;
        }
        Role role = SessionUtil.role(req);
        if (role == Role.PATIENT && next.startsWith("/patient/")) {
            return req.getContextPath() + next;
        }
        if (role == Role.DOCTOR && next.startsWith("/doctor/")) {
            return req.getContextPath() + next;
        }
        return null;
    }
}
