package com.pms.controller;

import com.pms.dao.StudentDao;
import com.pms.model.Student;
import com.pms.util.CsrfUtil;
import com.pms.util.DBConnection;
import com.pms.util.Eligibility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /admin/students — single-query list/search (name, roll, email, branch).
 * POST /admin/students with action=updateCgpa — admin-only CGPA edit for one
 * student (CSRF-checked). Role enforcement is done by AuthFilter.
 */
@WebServlet("/admin/students")
public class AdminStudentsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminStudentsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String q = request.getParameter("q");
        q = q == null ? "" : q.trim();
        if (q.length() > 100) {
            q = q.substring(0, 100);
        }
        try (Connection con = DBConnection.getConnection()) {
            List<Student> students = StudentDao.search(con, q);
            request.setAttribute("students", students);
            request.setAttribute("q", q);
            request.getRequestDispatcher("/admin/students.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin students list failed", ex);
            request.setAttribute("pageError", "Student list unavailable. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request.");
            return;
        }
        String redirect = request.getContextPath() + "/admin/students";
        String q = request.getParameter("q");
        if (q != null && !q.isBlank()) {
            redirect += "?q=" + URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
        }
        if (!"updateCgpa".equals(request.getParameter("action"))) {
            session.setAttribute("flashError", "Unknown action.");
            response.sendRedirect(redirect);
            return;
        }
        long studentId;
        try {
            studentId = Long.parseLong(request.getParameter("studentId"));
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError", "Invalid student.");
            response.sendRedirect(redirect);
            return;
        }
        BigDecimal cgpa;
        try {
            cgpa = Eligibility.parseCgpa(request.getParameter("cgpa"));
        } catch (IllegalArgumentException ex) {
            session.setAttribute("flashError", ex.getMessage());
            response.sendRedirect(redirect);
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            Student student = StudentDao.findByStudentId(con, studentId);
            if (student == null) {
                session.setAttribute("flashError", "Student not found.");
                response.sendRedirect(redirect);
                return;
            }
            int rows = StudentDao.updateCgpa(con, studentId, cgpa);
            if (rows == 1) {
                session.setAttribute("flashSuccess", "CGPA updated to "
                        + cgpa.toPlainString() + " for " + student.getRollNumber() + ".");
            } else {
                session.setAttribute("flashError", "CGPA update failed: student not found.");
            }
            response.sendRedirect(redirect);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin CGPA update failed", ex);
            session.setAttribute("flashError", "CGPA update failed due to a server error.");
            response.sendRedirect(redirect);
        }
    }
}
