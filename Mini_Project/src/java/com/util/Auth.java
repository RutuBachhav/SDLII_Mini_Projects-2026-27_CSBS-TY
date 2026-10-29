package com.util;

import com.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class Auth {

    public static final String SESSION_USER = "user";

    private Auth() {
    }

    public static User current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_USER);
        return value instanceof User user ? user : null;
    }

    public static void login(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static void flash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }

    public static void rememberReturn(HttpServletRequest request, String path) {
        if (path != null && path.startsWith("/") && !path.startsWith("//")) {
            request.getSession(true).setAttribute("returnTo", path);
        }
    }

    public static String takeReturn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "/home";
        }
        Object value = session.getAttribute("returnTo");
        session.removeAttribute("returnTo");
        if (value instanceof String path && path.startsWith("/") && !path.startsWith("//")) {
            return path;
        }
        return "/home";
    }

    public static User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = current(request);
        if (user == null) {
            String query = request.getQueryString();
            String path = request.getServletPath() + (query == null ? "" : "?" + query);
            rememberReturn(request, path);
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    public static User requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return null;
        }
        if (!"ADMIN".equals(user.getRole())) {
            flash(request, "err", "That page is for the studio admin account.");
            response.sendRedirect(request.getContextPath() + "/home");
            return null;
        }
        return user;
    }
}
