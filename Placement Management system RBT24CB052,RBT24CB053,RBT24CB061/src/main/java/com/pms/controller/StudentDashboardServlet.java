package com.pms.controller;

import com.pms.dao.ApplicationDao;
import com.pms.dao.CompanyDao;
import com.pms.dao.StudentDao;
import com.pms.model.ApplicationView;
import com.pms.model.Company;
import com.pms.model.Student;
import com.pms.util.AuthUtil;
import com.pms.util.DBConnection;
import com.pms.util.Eligibility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /student/dashboard. Loads profile, all companies and the student's own
 * applications (with results) in three queries, then computes per-company
 * eligibility (CGPA, branch, deadline, already-applied) in memory.
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentDashboardServlet.class.getName());

    /**
     * Per-company row for the drives table. Plain JavaBean (not a record):
     * GlassFish EL resolves {@code eligible}/{@code applied} only through
     * {@code isXxx()} getters.
     */
    public static final class CompanyRow {
        private final Company company;
        private final boolean eligible;
        private final String reason;
        private final boolean applied;

        public CompanyRow(Company company, boolean eligible, String reason, boolean applied) {
            this.company = company;
            this.eligible = eligible;
            this.reason = reason;
            this.applied = applied;
        }

        public Company getCompany() {
            return company;
        }

        public boolean isEligible() {
            return eligible;
        }

        public String getReason() {
            return reason;
        }

        public boolean isApplied() {
            return applied;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = AuthUtil.currentUserId(request.getSession(false));
        Long studentId = AuthUtil.currentStudentId(request.getSession(false));
        if (userId == null || studentId == null) {
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
            List<Company> companies = CompanyDao.listAll(con);
            List<ApplicationView> applications = ApplicationDao.listByStudentId(con, studentId);

            Map<Long, ApplicationView> appliedByCompany = new HashMap<>();
            for (ApplicationView a : applications) {
                appliedByCompany.put(a.getCompanyId(), a);
            }

            LocalDate today = LocalDate.now();
            List<CompanyRow> rows = new ArrayList<>();
            for (Company c : companies) {
                boolean applied = appliedByCompany.containsKey(c.getCompanyId());
                String reason = null;
                boolean eligible = !applied;
                if (applied) {
                    reason = "Already applied";
                } else if (!Eligibility.isDeadlineOpen(c.getApplicationDeadline(), today)) {
                    eligible = false;
                    reason = "Deadline passed";
                } else if (!Eligibility.isCgpaEligible(student.getCgpa(), c.getMinimumCgpa())) {
                    eligible = false;
                    reason = "CGPA below minimum";
                } else if (!Eligibility.isBranchEligible(student.getBranch(), c.getEligibleBranches())) {
                    eligible = false;
                    reason = "Branch not eligible";
                }
                rows.add(new CompanyRow(c, eligible, reason, applied));
            }

            request.setAttribute("student", student);
            request.setAttribute("companyRows", rows);
            request.setAttribute("applications", applications);
            request.getRequestDispatcher("/student/dashboard.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Student dashboard failed", ex);
            request.setAttribute("pageError", "Dashboard unavailable. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
