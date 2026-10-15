package com.web;

import com.dao.AuditDao;
import com.dao.UserDao;
import com.entity.User;
import com.util.Passwords;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Registration, login and logout. */
@WebServlet({"/register", "/login", "/logout"})
public class AuthServlet extends BaseServlet {

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final UserDao users = new UserDao();
    private final AuditDao audit = new AuditDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            logout(request, response);
            return;
        }
        if (Web.currentUser(request) != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        render(request, response, "/register".equals(path) ? "register.jsp" : "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/register".equals(path)) {
            register(request, response);
        } else if ("/login".equals(path)) {
            login(request, response);
        } else {
            logout(request, response);
        }
    }

    // ------------------------------------------------------------------ register

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = Web.str(request, "name");
        String email = Web.str(request, "email").toLowerCase();
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        List<String> errors = new ArrayList<>();
        if (name.length() < 3 || name.length() > 80) {
            errors.add("Enter your name, between 3 and 80 characters.");
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 160) {
            errors.add("Enter a valid email address.");
        }
        if (password == null || password.length() < 8) {
            errors.add("The password must be at least 8 characters long.");
        }
        if (password != null && !password.equals(confirm)) {
            errors.add("The two passwords do not match.");
        }

        try {
            if (errors.isEmpty() && users.emailExists(email)) {
                errors.add("An account with that email address already exists.");
            }
            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                request.setAttribute("name", name);
                request.setAttribute("email", email);
                render(request, response, "register.jsp");
                return;
            }
            // The role is not taken from the form: UserDao.create always writes USER.
            int id = users.create(name, email, Passwords.hash(password));
            audit.log(id, "USER_REGISTERED", "user", id, email);
            User user = users.findById(id);
            startSession(request, user);
            redirect(request, response, "/dashboard", "success",
                    "Welcome to FoundIt, " + user.getName() + ".");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    // ------------------------------------------------------------------ login

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = Web.str(request, "email").toLowerCase();
        String password = request.getParameter("password");
        try {
            String hash = users.passwordHashFor(email);
            // Same message either way, so the form does not reveal which
            // addresses are registered.
            if (hash == null || !Passwords.matches(password, hash)) {
                request.setAttribute("errors",
                        List.of("That email address and password do not match an account."));
                request.setAttribute("email", email);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                render(request, response, "login.jsp");
                return;
            }
            User user = users.findByEmail(email);
            startSession(request, user);
            String next = Web.str(request, "next");
            redirect(request, response,
                    next.startsWith("/") && !next.startsWith("//") ? next : "/dashboard",
                    "success", "Signed in as " + user.getName() + ".");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect(request, response, "/", "note", "You have been signed out.");
    }

    /** A fresh session id on sign-in, so a fixated session cannot be reused. */
    private void startSession(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(Web.SESSION_USER, user);
        session.setMaxInactiveInterval(60 * 60);
    }
}
