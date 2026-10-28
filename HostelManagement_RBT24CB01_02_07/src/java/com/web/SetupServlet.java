package com.web;

import com.security.AuthorizationService;
import com.security.BootstrapListener;
import com.security.PasswordUtil;
import com.security.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * One-time first-Admin bootstrap. Disables itself permanently as soon as any
 * user account exists.
 */
@WebServlet("/setup")
public class SetupServlet extends HttpServlet {

    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z0-9._-]{3,60}$");

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (initialized(request, response)) {
            return;
        }
        ensureSetupKey(request);
        request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (initialized(request, response)) {
            return;
        }

        String expected = setupKey(request);
        String supplied = request.getParameter("setupKey");
        if (expected == null || supplied == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                supplied.trim().getBytes(StandardCharsets.UTF_8))) {
            request.setAttribute("error",
                    "Setup could not be completed with the supplied key.");
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");
        String cleanUsername = username == null ? "" : username.trim();

        request.setAttribute("username", cleanUsername);

        if (!USERNAME.matcher(cleanUsername).matches()) {
            request.setAttribute("error",
                    "Username must be 3-60 characters using letters, digits, "
                    + "dot, underscore or hyphen.");
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        }

        String passwordProblem = PasswordUtil.strengthProblem(password);
        if (passwordProblem != null) {
            request.setAttribute("error", passwordProblem);
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirm)) {
            request.setAttribute("error", "The passwords do not match.");
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        }

        try {
            users.ensureRoles();
            users.createUser(cleanUsername, password,
                    AuthorizationService.ROLE_ADMIN);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        } catch (RuntimeException ex) {
            getServletContext().log("RBAC bootstrap failed", ex);
            request.setAttribute("error",
                    "Setup could not be completed. Check the server log.");
            request.getRequestDispatcher("/WEB-INF/setup.jsp").forward(request, response);
            return;
        }

        getServletContext().removeAttribute(BootstrapListener.SETUP_KEY_ATTR);
        getServletContext().log(
                "RBAC bootstrap completed; the one-time setup key was removed.");
        response.sendRedirect(request.getContextPath() + "/login?setup=1");
    }

    private boolean initialized(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException {
        try {
            if (users.countUsers() > 0) {
                SimplePage.render(request, response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Setup Unavailable",
                        "Setup is not available",
                        "This application already has user accounts.",
                        "/login", "Go to Sign In");
                return true;
            }
            return false;
        } catch (RuntimeException ex) {
            getServletContext().log("Setup check failed", ex);
            SimplePage.render(request, response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Setup Unavailable",
                    "Database is unavailable",
                    "Setup cannot start until the database is reachable.",
                    "/login", "Try Sign In");
            return true;
        }
    }

    private void ensureSetupKey(HttpServletRequest request) {
        if (setupKey(request) != null) {
            return;
        }
        String key = BootstrapListener.generateKey();
        request.getServletContext().setAttribute(
                BootstrapListener.SETUP_KEY_ATTR, key);
        request.getServletContext().log("RBAC_SETUP_KEY=" + key
                + " (one-time bootstrap key; open /setup)");
    }

    private String setupKey(HttpServletRequest request) {
        Object value = request.getServletContext()
                .getAttribute(BootstrapListener.SETUP_KEY_ATTR);
        if (value instanceof String && !((String) value).isEmpty()) {
            return (String) value;
        }
        String configured = System.getProperty(
                BootstrapListener.SETUP_KEY_PROPERTY);
        return configured == null || configured.trim().isEmpty()
                ? null : configured.trim();
    }
}
