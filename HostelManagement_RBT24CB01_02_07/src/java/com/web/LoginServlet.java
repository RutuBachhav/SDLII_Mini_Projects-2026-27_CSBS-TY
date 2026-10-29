package com.web;

import com.entity.AppRole;
import com.entity.AppUser;
import com.security.AuthorizationService;
import com.security.LoginRateLimiter;
import com.security.PasswordUtil;
import com.security.SessionAuth;
import com.security.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String GENERIC_ERROR =
            "Invalid username or password.";

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionAuth.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String cleanUsername = username == null ? "" : username.trim();

        request.setAttribute("username", cleanUsername);

        if (cleanUsername.isEmpty() || password == null
                || password.isEmpty()) {
            fail(request, response, GENERIC_ERROR);
            return;
        }

        String limitKey = cleanUsername.toLowerCase(Locale.ROOT)
                + "|" + request.getRemoteAddr();

        if (LoginRateLimiter.isBlocked(limitKey)) {
            fail(request, response,
                    "Too many failed attempts. Try again in a few minutes.");
            return;
        }

        AppUser user;
        try {
            user = users.findByUsername(cleanUsername);
        } catch (RuntimeException ex) {
            getServletContext().log("Login lookup failed", ex);
            fail(request, response,
                    "Sign-in is temporarily unavailable. Try again.");
            return;
        }

        boolean passwordOk;
        if (user == null) {
            PasswordUtil.verify(password, PasswordUtil.dummyHash());
            passwordOk = false;
        } else {
            passwordOk = PasswordUtil.verify(password, user.getPasswordHash());
        }

        List<String> roleNames = new ArrayList<>();
        if (user != null && user.getRoles() != null) {
            for (AppRole role : user.getRoles()) {
                if (role != null && role.getRoleName() != null) {
                    roleNames.add(role.getRoleName()
                            .toUpperCase(Locale.ROOT));
                }
            }
        }

        boolean valid = user != null
                && user.isActive()
                && passwordOk
                && AuthorizationService.highestRole(roleNames) != null;

        if (!valid) {
            LoginRateLimiter.recordFailure(limitKey);
            fail(request, response, GENERIC_ERROR);
            return;
        }

        LoginRateLimiter.clear(limitKey);

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        SessionAuth.login(session, user);

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    private void fail(HttpServletRequest request,
                      HttpServletResponse response,
                      String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
