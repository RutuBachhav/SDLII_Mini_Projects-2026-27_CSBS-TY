package com.hospital.web;

import com.hospital.module.AppUser;
import com.hospital.module.HospitalDAO;
import com.hospital.module.Role;
import com.hospital.module.ValidationUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Public registration. Always creates a PATIENT account — role is never read from the client. */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (SessionUtil.userId(req) != null) {
            res.sendRedirect(homeFor(SessionUtil.role(req), req));
            return;
        }
        req.setAttribute("pageTitle", "Create your account");
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String name = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        String nameError = null, emailError = null, passwordError = null, confirmError = null;
        if (!ValidationUtil.isName(name)) {
            nameError = "Enter your full name (at least 2 characters).";
        }
        if (!ValidationUtil.isEmail(email)) {
            emailError = "Enter a valid email address, for example name@example.com.";
        } else if (HospitalDAO.emailTaken(email)) {
            emailError = "An account with this email already exists. Try signing in instead.";
        }
        if (!ValidationUtil.isPassword(password)) {
            passwordError = "Use at least 8 characters with both letters and numbers.";
        }
        if (confirm == null || !confirm.equals(password)) {
            confirmError = "Passwords do not match.";
        }

        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            req.setAttribute("pageTitle", "Create your account");
            req.setAttribute("nameError", nameError);
            req.setAttribute("emailError", emailError);
            req.setAttribute("passwordError", passwordError);
            req.setAttribute("confirmError", confirmError);
            req.setAttribute("fullName", name);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        try {
            AppUser user = HospitalDAO.registerPatient(name, email, password);
            SessionUtil.login(req, user);
            res.sendRedirect(req.getContextPath() + "/patient/dashboard");
        } catch (IllegalStateException e) {
            req.setAttribute("pageTitle", "Create your account");
            req.setAttribute("emailError", e.getMessage());
            req.setAttribute("fullName", name);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
        }
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }

    static String homeFor(Role role, HttpServletRequest req) {
        if (role == Role.DOCTOR) {
            return req.getContextPath() + "/doctor/dashboard";
        }
        return req.getContextPath() + "/patient/dashboard";
    }
}
