package com.pms.controller;

import com.pms.dao.ApplicationDao;
import com.pms.dao.CompanyDao;
import com.pms.dao.StudentDao;
import com.pms.model.Company;
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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POST /student/apply. Re-checks company existence, deadline, CGPA, branch
 * and duplicate application server-side inside one transaction.
 */
@WebServlet("/student/apply")
public class ApplyServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ApplyServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Long userId = AuthUtil.currentUserId(session);
        Long studentId = AuthUtil.currentStudentId(session);
        if (userId == null || studentId == null) {
            response.sendRedirect(request.getContextPath() + "/student-login.jsp");
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request.");
            return;
        }

        long companyId;
        try {
            companyId = Long.parseLong(request.getParameter("companyId"));
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError", "Invalid company.");
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Student student = StudentDao.findByUserId(con, userId);
                if (student == null || student.getStudentId() != studentId) {
                    con.rollback();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
                Company company = CompanyDao.findById(con, companyId);
                if (company == null) {
                    con.rollback();
                    session.setAttribute("flashError", "Company no longer exists.");
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                    return;
                }
                String denial = checkEligibility(student, company);
                if (denial != null) {
                    con.rollback();
                    session.setAttribute("flashError", denial);
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                    return;
                }
                if (ApplicationDao.exists(con, studentId, companyId)) {
                    con.rollback();
                    session.setAttribute("flashError", "You have already applied to this company.");
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                    return;
                }
                ApplicationDao.insert(con, studentId, companyId);
                con.commit();
                session.setAttribute("flashSuccess",
                        "Applied to " + company.getCompanyName() + " successfully.");
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            } catch (Exception ex) {
                try {
                    con.rollback();
                } catch (Exception rb) {
                    LOG.log(Level.WARNING, "Rollback failed", rb);
                }
                throw ex;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Application failed", ex);
            session.setAttribute("flashError", "Application failed. Please try again.");
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    }

    private static String checkEligibility(Student student, Company company) {
        if (!Eligibility.isDeadlineOpen(company.getApplicationDeadline(), LocalDate.now())) {
            return "Application deadline has passed.";
        }
        if (!Eligibility.isCgpaEligible(student.getCgpa(), company.getMinimumCgpa())) {
            return "Your CGPA does not meet the minimum requirement.";
        }
        if (!Eligibility.isBranchEligible(student.getBranch(), company.getEligibleBranches())) {
            return "Your branch is not eligible for this drive.";
        }
        return null;
    }
}
