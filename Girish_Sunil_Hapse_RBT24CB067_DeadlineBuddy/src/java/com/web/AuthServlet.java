package com.web;

import com.entity.User;
import com.service.AuthService;
import com.util.AppException;
import com.util.Config;
import com.util.ValidationException;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Sign-in, registration and sign-out.
 *
 * Only the user's id, name and email go in the session - never the password or
 * its hash. The session id is replaced on every successful sign-in, so a
 * session id an attacker managed to plant before sign-in is not the one that
 * ends up authenticated.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            // Sign-out is a POST. A GET here most likely means a bookmarked
            // link or a prefetch, which must not end someone's session.
            redirect(req, res, "/login");
            return;
        }

        if (Web.currentUserId(req) != null) {
            redirect(req, res, "/dashboard");
            return;
        }

        req.setAttribute("next", safeNext(req, req.getParameter("next")));
        render(req, res, "/register".equals(path) ? "register" : "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        switch (req.getServletPath()) {
            case "/login":
                login(req, res);
                break;
            case "/register":
                register(req, res);
                break;
            case "/logout":
                logout(req, res);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = Web.strOrEmpty(req, "email");
        String next = safeNext(req, req.getParameter("next"));
        try {
            User user = auth.login(email, req.getParameter("password"));
            startSession(req, user);
            Web.flashSuccess(req, "Welcome back, " + user.getFullName() + ".");
            res.sendRedirect(req.getContextPath() + next);
        } catch (AppException e) {
            // Redisplay with the email kept but the password cleared.
            req.setAttribute("email", email);
            req.setAttribute("next", next);
            req.setAttribute("errorSummary", e.getMessage());
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            render(req, res, "login");
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String fullName = Web.strOrEmpty(req, "fullName");
        String email = Web.strOrEmpty(req, "email");
        try {
            User user = auth.register(fullName, email,
                    req.getParameter("password"), req.getParameter("confirm"));
            startSession(req, user);
            Web.flashSuccess(req, "Your account is ready. Start by adding a subject.");
            redirect(req, res, "/subjects?welcome=1");
        } catch (ValidationException e) {
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            render(req, res, "register");
        }
    }

    private void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Web.flashSuccess(req, "You have been signed out.");
        redirect(req, res, "/login");
    }

    /**
     * Starts a fresh session for a user who has just proved who they are.
     * The old session is discarded first, which is what defeats session
     * fixation.
     */
    private void startSession(HttpServletRequest req, User user) {
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(Config.sessionTimeoutMinutes() * 60);
        session.setAttribute(Web.SESSION_USER_ID, user.getId());
        session.setAttribute(Web.SESSION_USER_NAME, user.getFullName());
        session.setAttribute(Web.SESSION_USER_EMAIL, user.getEmail());
    }

    /**
     * Sanitises the {@code next} parameter.
     *
     * Only a path inside this application is allowed through. Anything with a
     * scheme, a host, a backslash or a leading double slash would be an open
     * redirect, so it is dropped and the dashboard is used instead.
     */
    private String safeNext(HttpServletRequest req, String raw) {
        String fallback = "/dashboard";
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        String next = raw.trim();
        if (next.contains(":") || next.contains("\\") || next.startsWith("//")) {
            return fallback;
        }
        String context = req.getContextPath();
        if (!context.isEmpty() && next.startsWith(context)) {
            next = next.substring(context.length());
        }
        if (!next.startsWith("/") || next.startsWith("//")) {
            return fallback;
        }
        // Signing in only ever continues to a page, never to a POST endpoint.
        if (next.startsWith("/dashboard") || next.startsWith("/tasks") || next.startsWith("/subjects")) {
            return next;
        }
        return fallback;
    }
}
