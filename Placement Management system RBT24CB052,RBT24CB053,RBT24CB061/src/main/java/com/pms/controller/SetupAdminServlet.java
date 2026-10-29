package com.pms.controller;

import com.pms.dao.UserDao;
import com.pms.util.DBConnection;
import com.pms.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * One-time admin bootstrap. Renders only when zero ADMIN users exist AND a
 * setup token (env PMS_ADMIN_SETUP_TOKEN) is configured; the posted token must
 * match. After the first admin exists the endpoint self-disables (404).
 * There is deliberately NO public admin registration.
 */
@WebServlet("/admin-setup")
public class SetupAdminServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SetupAdminServlet.class.getName());
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private boolean setupOpen() {
        String token = System.getenv("PMS_ADMIN_SETUP_TOKEN");
        if (token == null || token.isBlank()) {
            return false;
        }
        try (Connection con = DBConnection.getConnection()) {
            return UserDao.countAdmins(con) == 0;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin setup availability check failed", ex);
            return false;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!setupOpen()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String expected = System.getenv("PMS_ADMIN_SETUP_TOKEN");
        String provided = request.getParameter("setupToken");
        boolean tokenOk = expected != null && !expected.isBlank() && provided != null
                && MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        provided.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (!tokenOk) {
            request.setAttribute("setupError", "Invalid setup token.");
            request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
            return;
        }

        String email = request.getParameter("email");
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String password = request.getParameter("password");
        password = password == null ? "" : password;

        if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("setupError", "Enter a valid email address.");
            request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
            return;
        }
        if (password.length() < 8 || password.length() > 128) {
            request.setAttribute("setupError", "Password must be 8-128 characters.");
            request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
            return;
        }

        char[] pwChars = password.toCharArray();
        password = null;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (UserDao.countAdmins(con) != 0) {
                    con.rollback();
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (UserDao.findByEmail(con, email) != null) {
                    con.rollback();
                    request.setAttribute("setupError", "This email is already registered.");
                    request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
                    return;
                }
                String hash = PasswordUtil.hash(pwChars);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'ADMIN')")) {
                    ps.setString(1, email);
                    ps.setString(2, hash);
                    ps.executeUpdate();
                }
                con.commit();
                LOG.log(Level.INFO, "Initial admin account created.");
                request.setAttribute("setupSuccess",
                        "Admin account created. You can now log in.");
                request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
            } catch (Exception ex) {
                try {
                    con.rollback();
                } catch (Exception rb) {
                    LOG.log(Level.WARNING, "Rollback failed", rb);
                }
                LOG.log(Level.SEVERE, "Admin setup failed", ex);
                request.setAttribute("setupError",
                        "Setup failed due to a server error. Please try again.");
                request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin setup failed (connection)", ex);
            request.setAttribute("setupError", "Setup failed due to a server error. Please try again.");
            request.getRequestDispatcher("/admin-setup.jsp").forward(request, response);
        } finally {
            Arrays.fill(pwChars, '\0');
        }
    }
}
