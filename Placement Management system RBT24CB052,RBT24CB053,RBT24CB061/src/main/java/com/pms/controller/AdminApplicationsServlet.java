package com.pms.controller;

import com.pms.dao.ApplicationDao;
import com.pms.dao.CompanyDao;
import com.pms.model.ApplicationView;
import com.pms.model.Company;
import com.pms.util.CsrfUtil;
import com.pms.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /admin/applications lists applications (optional status/company filter).
 * POST updates one application status plus its placement_results row in a
 * single transaction (CSRF-checked).
 */
@WebServlet("/admin/applications")
public class AdminApplicationsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminApplicationsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = trim(request.getParameter("status"));
        String companyParam = trim(request.getParameter("companyId"));
        Long companyId = null;
        if (!companyParam.isEmpty()) {
            try {
                companyId = Long.parseLong(companyParam);
            } catch (NumberFormatException ex) {
                LOG.log(Level.WARNING, "Bad company filter", ex);
            }
        }
        if (!status.isEmpty() && !ApplicationDao.STATUSES.contains(status)) {
            status = "";
        }
        try (Connection con = DBConnection.getConnection()) {
            List<ApplicationView> applications =
                    ApplicationDao.listAll(con, status.isEmpty() ? null : status, companyId);
            List<Company> companies = CompanyDao.listAll(con);
            request.setAttribute("applications", applications);
            request.setAttribute("companies", companies);
            request.setAttribute("filterStatus", status);
            request.setAttribute("filterCompany", companyParam);
            request.setAttribute("statuses", ApplicationDao.STATUSES);
            request.getRequestDispatcher("/admin/applications.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin applications list failed", ex);
            request.setAttribute("pageError", "Applications unavailable. Please try again later.");
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
        long applicationId;
        String status = trim(request.getParameter("status"));
        if (!ApplicationDao.STATUSES.contains(status)) {
            session.setAttribute("flashError", "Invalid status.");
            response.sendRedirect(request.getContextPath() + "/admin/applications");
            return;
        }
        try {
            applicationId = Long.parseLong(request.getParameter("applicationId"));
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError", "Invalid application.");
            response.sendRedirect(request.getContextPath() + "/admin/applications");
            return;
        }
        Date interviewDate = null;
        String dateRaw = trim(request.getParameter("interviewDate"));
        if (!dateRaw.isEmpty()) {
            try {
                interviewDate = Date.valueOf(LocalDate.parse(dateRaw));
            } catch (Exception ex) {
                session.setAttribute("flashError", "Invalid interview date (use yyyy-MM-dd).");
                response.sendRedirect(request.getContextPath() + "/admin/applications");
                return;
            }
        }
        String remarks = trim(request.getParameter("remarks"));
        if (remarks.length() > 500) {
            remarks = remarks.substring(0, 500);
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int rows = ApplicationDao.updateStatus(con, applicationId, status);
                if (rows != 1) {
                    con.rollback();
                    session.setAttribute("flashError", "Application not found.");
                } else {
                    ApplicationDao.syncResult(con, applicationId, status, interviewDate,
                            remarks.isEmpty() ? null : remarks);
                    con.commit();
                    session.setAttribute("flashSuccess",
                            "Application #" + applicationId + " set to " + status + ".");
                }
                response.sendRedirect(request.getContextPath() + "/admin/applications");
            } catch (Exception ex) {
                try {
                    con.rollback();
                } catch (Exception rb) {
                    LOG.log(Level.WARNING, "Rollback failed", rb);
                }
                throw ex;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Application status update failed", ex);
            session.setAttribute("flashError", "Update failed due to a server error.");
            response.sendRedirect(request.getContextPath() + "/admin/applications");
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
