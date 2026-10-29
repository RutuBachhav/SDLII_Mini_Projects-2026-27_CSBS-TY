package com.pms.controller;

import com.pms.util.DBConnection;
import com.pms.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Handles student registration (POST /student-register).
 *
 * <p>Inserts into {@code users} (role STUDENT) then {@code students} in a single
 * JDBC transaction using the generated {@code users.user_id} key. Passwords are
 * hashed with PBKDF2WithHmacSHA256; plaintext is never stored or logged, and
 * SQL details are never sent to the browser.
 */
@WebServlet("/student-register")
public class StudentRegistrationServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentRegistrationServlet.class.getName());

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ROLL_PATTERN =
            Pattern.compile("^[A-Za-z0-9/_-]{2,30}$");

    private static final int MIN_PASSWORD_LEN = 8;
    private static final int MAX_PASSWORD_LEN = 128;
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // ---- Read & normalize (passwords intentionally NOT trimmed) ----
        String fullName = normalizeName(request.getParameter("fullName"));
        String email = normalizeEmail(request.getParameter("email"));
        String rollNumber = normalizeRoll(request.getParameter("rollNumber"));
        String branch = normalizeName(request.getParameter("branch"));
        String cgpaStr = trim(request.getParameter("cgpa"));
        String yearStr = trim(request.getParameter("graduationYear"));
        String password = nullToEmpty(request.getParameter("password"));
        String confirm = nullToEmpty(request.getParameter("confirmPassword"));

        Map<String, String> errors = new LinkedHashMap<>();
        repopulate(request, fullName, email, rollNumber, branch, cgpaStr, yearStr);

        // ---- Validate ----
        if (fullName.isEmpty()) {
            errors.put("fullName", "Full name is required.");
        } else if (fullName.length() < 2 || fullName.length() > 100) {
            errors.put("fullName", "Full name must be 2-100 characters.");
        }

        if (email.isEmpty()) {
            errors.put("email", "Email is required.");
        } else if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Enter a valid email address.");
        }

        if (rollNumber.isEmpty()) {
            errors.put("rollNumber", "Roll number is required.");
        } else if (!ROLL_PATTERN.matcher(rollNumber).matches()) {
            errors.put("rollNumber", "Roll number may contain only letters, digits, /, _ or - (2-30 chars).");
        }

        if (branch.isEmpty()) {
            errors.put("branch", "Branch is required.");
        } else if (branch.length() > 50) {
            errors.put("branch", "Branch must be 50 characters or fewer.");
        }

        BigDecimal cgpa = null;
        if (cgpaStr.isEmpty()) {
            errors.put("cgpa", "CGPA is required.");
        } else {
            try {
                double d = Double.parseDouble(cgpaStr);
                if (!Double.isFinite(d) || d < 0 || d > 10) {
                    errors.put("cgpa", "CGPA must be between 0 and 10.");
                } else {
                    cgpa = BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
                }
            } catch (NumberFormatException ex) {
                errors.put("cgpa", "CGPA must be a number between 0 and 10.");
            }
        }

        Integer gradYear = null;
        if (yearStr.isEmpty()) {
            errors.put("graduationYear", "Graduation year is required.");
        } else {
            try {
                int y = Integer.parseInt(yearStr);
                if (y < MIN_YEAR || y > MAX_YEAR) {
                    errors.put("graduationYear", "Graduation year must be between "
                            + MIN_YEAR + " and " + MAX_YEAR + ".");
                } else {
                    gradYear = y;
                }
            } catch (NumberFormatException ex) {
                errors.put("graduationYear", "Graduation year must be a valid year.");
            }
        }

        if (password.isEmpty()) {
            errors.put("password", "Password is required.");
        } else if (password.length() < MIN_PASSWORD_LEN || password.length() > MAX_PASSWORD_LEN) {
            errors.put("password", "Password must be 8-128 characters.");
        } else if (!password.equals(confirm)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
            return;
        }

        // ---- Hash + transactional insert ----
        char[] pwChars = password.toCharArray();
        // Drop String references ASAP; the char copy is cleared in finally.
        password = null;
        confirm = null;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (emailExists(con, email)) {
                    errors.put("email", "This email is already registered.");
                }
                if (rollExists(con, rollNumber)) {
                    errors.put("rollNumber", "This roll number is already registered.");
                }
                if (!errors.isEmpty()) {
                    con.rollback();
                    request.setAttribute("errors", errors);
                    request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
                    return;
                }

                String passwordHash = PasswordUtil.hash(pwChars);

                long userId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'STUDENT')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, email);
                    ps.setString(2, passwordHash);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("User insert affected 0 rows.");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to obtain generated user_id.");
                        }
                        userId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students (user_id, roll_number, full_name, branch, cgpa, graduation_year)"
                                + " VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setLong(1, userId);
                    ps.setString(2, rollNumber);
                    ps.setString(3, fullName);
                    ps.setString(4, branch);
                    if (cgpa != null) {
                        ps.setBigDecimal(5, cgpa);
                    } else {
                        ps.setNull(5, Types.DECIMAL);
                    }
                    if (gradYear != null) {
                        ps.setInt(6, gradYear);
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Student insert affected 0 rows.");
                    }
                }

                con.commit();
                // Success: clear repopulated values so the form resets; expose only safe data.
                clearForm(request);
                request.setAttribute("success",
                        "Registration successful for " + email + ". You can now log in.");
                LOG.log(Level.INFO, "Student registered: user_id={0}", userId);
                request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
            } catch (SQLIntegrityConstraintViolationException dup) {
                // Race between pre-check and insert: map to a friendly field message.
                rollbackQuietly(con);
                LOG.log(Level.WARNING, "Duplicate registration attempt", dup);
                resolveDuplicate(con, email, rollNumber, errors);
                if (errors.isEmpty()) {
                    errors.put("form", "This email or roll number is already registered.");
                }
                request.setAttribute("errors", errors);
                request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
            } catch (SQLException ex) {
                rollbackQuietly(con);
                // Never send SQL text/state to the browser.
                LOG.log(Level.SEVERE, "Student registration failed", ex);
                errors.put("form", "Registration failed due to a server error. Please try again.");
                request.setAttribute("errors", errors);
                request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
            } catch (Exception ex) {
                rollbackQuietly(con);
                LOG.log(Level.SEVERE, "Student registration failed", ex);
                errors.put("form", "Registration failed due to a server error. Please try again.");
                request.setAttribute("errors", errors);
                request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Student registration failed (connection)", ex);
            errors.put("form", "Registration failed due to a server error. Please try again.");
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/student-registration.jsp").forward(request, response);
        } finally {
            Arrays.fill(pwChars, '\0');
        }
    }

    // ---------- helpers ----------

    private static boolean emailExists(Connection con, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean rollExists(Connection con, String roll) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM students WHERE roll_number = ?")) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void resolveDuplicate(Connection con, String email, String roll,
            Map<String, String> errors) {
        try {
            if (emailExists(con, email)) {
                errors.put("email", "This email is already registered.");
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Duplicate resolution check failed", ex);
        }
        try {
            if (rollExists(con, roll)) {
                errors.put("rollNumber", "This roll number is already registered.");
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Duplicate resolution check failed", ex);
        }
    }

    private static void rollbackQuietly(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Rollback failed", ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String normalizeName(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeEmail(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeRoll(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static void repopulate(HttpServletRequest request, String fullName, String email,
            String roll, String branch, String cgpa, String year) {
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formRollNumber", roll);
        request.setAttribute("formBranch", branch);
        request.setAttribute("formCgpa", cgpa);
        request.setAttribute("formGraduationYear", year);
    }

    private static void clearForm(HttpServletRequest request) {
        request.setAttribute("formFullName", "");
        request.setAttribute("formEmail", "");
        request.setAttribute("formRollNumber", "");
        request.setAttribute("formBranch", "");
        request.setAttribute("formCgpa", "");
        request.setAttribute("formGraduationYear", "");
    }
}
