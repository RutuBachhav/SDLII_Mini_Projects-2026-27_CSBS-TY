package com.pms.controller;

import com.pms.dao.StudentDao;
import com.pms.model.Student;
import com.pms.util.AuthUtil;
import com.pms.util.CsrfUtil;
import com.pms.util.DBConnection;
import com.pms.util.Eligibility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET shows the profile form; POST updates permitted fields only
 * (full_name, phone, skills, cgpa). Branch/graduation year are read-only.
 */
@WebServlet("/student/profile")
public class StudentProfileServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentProfileServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = AuthUtil.currentUserId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/student-login.jsp");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            Student student = StudentDao.findByUserId(con, userId);
            if (student == null) {
                request.setAttribute("pageError", "Student profile not found.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            request.setAttribute("student", student);
            request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Student profile load failed", ex);
            request.setAttribute("pageError", "Profile unavailable. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long userId = AuthUtil.currentUserId(request.getSession(false));
        Long studentId = AuthUtil.currentStudentId(request.getSession(false));
        if (userId == null || studentId == null) {
            response.sendRedirect(request.getContextPath() + "/student-login.jsp");
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request.");
            return;
        }

        String fullName = clean(request.getParameter("fullName"));
        String phone = clean(request.getParameter("phone"));
        String skills = clean(request.getParameter("skills"));

        Map<String, String> errors = new LinkedHashMap<>();
        if (fullName.isEmpty() || fullName.length() < 2 || fullName.length() > 100) {
            errors.put("fullName", "Full name must be 2-100 characters.");
        }
        if (!phone.isEmpty() && !phone.matches("[+0-9()\\-\\.\\s]{7,15}")) {
            errors.put("phone", "Enter a valid phone number (7-15 digits).");
        }
        if (skills.length() > 2000) {
            skills = skills.substring(0, 2000);
        }
        BigDecimal cgpa = null;
        try {
            cgpa = Eligibility.parseCgpa(request.getParameter("cgpa"));
        } catch (IllegalArgumentException ex) {
            errors.put("cgpa", ex.getMessage());
        }

        try (Connection con = DBConnection.getConnection()) {
            Student student = StudentDao.findByUserId(con, userId);
            if (student == null || student.getStudentId() != studentId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                // Re-render the user's typed values (not the stored ones) so
                // input is preserved; read-only columns come from the database.
                request.setAttribute("student", new Student(student.getStudentId(),
                        student.getUserId(), student.getRollNumber(), fullName,
                        student.getBranch(), cgpa, student.getGraduationYear(),
                        skills, phone, student.getEmail()));
                request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
                return;
            }
            con.setAutoCommit(false);
            try {
                StudentDao.updateProfile(con, studentId, fullName,
                        phone.isEmpty() ? null : phone, skills.isEmpty() ? null : skills);
                StudentDao.updateCgpa(con, studentId, cgpa);
                con.commit();
            } catch (Exception ex) {
                try {
                    con.rollback();
                } catch (Exception rb) {
                    LOG.log(Level.WARNING, "Rollback failed", rb);
                }
                throw ex;
            }
            request.setAttribute("success", "Profile updated successfully. Your CGPA is now " + cgpa.toPlainString() + ".");
            request.setAttribute("student", StudentDao.findByUserId(con, userId));
            request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Student profile update failed", ex);
            request.setAttribute("pageError", "Update failed due to a server error.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
