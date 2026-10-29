package com.web;

import com.dao.UserDAO;
import com.entity.User;
import com.util.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Auth.current(request) != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        String error = validate(name, email, phone, password, confirm);
        if (error != null) {
            keep(request, name, email, phone, error);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.register(name, email, phone, password);
            Auth.login(request, user);
            Auth.flash(request, "ok", "Account created. You are signed in.");
            response.sendRedirect(request.getContextPath() + Auth.takeReturn(request));
        } catch (SQLException e) {
            String message = e.getErrorCode() == 1062
                    ? "An account with this email already exists."
                    : "Could not create the account. " + e.getMessage();
            keep(request, name, email, phone, message);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private String validate(String name, String email, String phone, String password, String confirm) {
        if (name.length() < 2) {
            return "Enter your name.";
        }
        if (!email.contains("@") || !email.contains(".")) {
            return "Enter a valid email.";
        }
        if (!phone.isEmpty() && !phone.matches("[0-9 +()-]{8,20}")) {
            return "Enter a valid phone number, or leave it blank.";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }
        return null;
    }

    private void keep(HttpServletRequest request, String name, String email, String phone, String error) {
        request.setAttribute("error", error);
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
