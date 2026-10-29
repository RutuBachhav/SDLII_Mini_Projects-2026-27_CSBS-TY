package com.pms.controller;

import com.pms.dao.StudentDao;
import com.pms.dao.UserDao;
import com.pms.model.Student;
import com.pms.util.AuthUtil;
import com.pms.util.DBConnection;
import com.pms.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Student + admin login. GET shows the matching form; POST verifies the
 * password hash with {@link PasswordUtil#verify} and enforces the expected
 * role. Session fixation is prevented by invalidating the pre-login session.
 */
@WebServlet(urlPatterns = {"/student-login", "/admin-login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String GENERIC_ERROR = "Invalid email or password.";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean admin = request.getServletPath().equals("/admin-login");
        HttpSession existing = request.getSession(false);
        if (admin && AuthUtil.isAdmin(existing)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        if (!admin && AuthUtil.isStudent(existing)) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }
        request.getRequestDispatcher(admin ? "/admin-login.jsp" : "/student-login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        boolean admin = request.getServletPath().equals("/admin-login");
        String expectedRole = admin ? AuthUtil.ROLE_ADMIN : AuthUtil.ROLE_STUDENT;
        String jsp = admin ? "/admin-login.jsp" : "/student-login.jsp";

        String email = request.getParameter("email");
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String password = request.getParameter("password");
        password = password == null ? "" : password;

        if (email.isEmpty() || password.isEmpty() || email.length() > 100) {
            request.setAttribute("loginError", GENERIC_ERROR);
            request.getRequestDispatcher(jsp).forward(request, response);
            return;
        }

        char[] pwChars = password.toCharArray();
        password = null;
        try (Connection con = DBConnection.getConnection()) {
            UserDao.UserRow row = UserDao.findByEmail(con, email);
            boolean ok = row != null && expectedRole.equals(row.role());
            if (ok) {
                try {
                    ok = PasswordUtil.verify(pwChars, row.passwordHash());
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Password verification failed", ex);
                    ok = false;
                }
            }
            if (!ok) {
                // Same message for unknown email, wrong password or wrong role:
                // no user enumeration, no role disclosure.
                request.setAttribute("loginError", GENERIC_ERROR);
                request.setAttribute("formEmail", email);
                request.getRequestDispatcher(jsp).forward(request, response);
                return;
            }

            Long studentId = null;
            if (!admin) {
                Student s = StudentDao.findByUserId(con, row.userId());
                if (s == null) {
                    LOG.log(Level.WARNING, "Student profile missing for user");
                    request.setAttribute("loginError", GENERIC_ERROR);
                    request.getRequestDispatcher(jsp).forward(request, response);
                    return;
                }
                studentId = s.getStudentId();
            }

            // Session fixation protection: capture redirect target, then drop
            // the pre-login session entirely.
            HttpSession old = request.getSession(false);
            String next = old == null ? null : (String) old.getAttribute("postLoginNext");
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(AuthUtil.ATTR_USER_ID, row.userId());
            session.setAttribute(AuthUtil.ATTR_ROLE, row.role());
            session.setAttribute(AuthUtil.ATTR_EMAIL, row.email());
            if (studentId != null) {
                session.setAttribute(AuthUtil.ATTR_STUDENT_ID, studentId);
            }
            session.setMaxInactiveInterval(30 * 60);

            String target;
            if (next != null && next.startsWith(admin ? "/admin/" : "/student/")) {
                target = next;
            } else {
                target = admin ? "/admin/dashboard" : "/student/dashboard";
            }
            response.sendRedirect(request.getContextPath() + target);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Login failed", ex);
            request.setAttribute("loginError", "Login failed due to a server error. Please try again.");
            request.getRequestDispatcher(jsp).forward(request, response);
        } finally {
            Arrays.fill(pwChars, '\0');
        }
    }
}
