package com.pms.controller;

import com.pms.dao.CompanyDao;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /admin/companies lists companies. POST with action=add|update|delete
 * mutates (CSRF-checked). Deletes are blocked when applications reference the
 * company; GET is never used for deletion.
 */
@WebServlet("/admin/companies")
public class AdminCompanyServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminCompanyServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection con = DBConnection.getConnection()) {
            List<Company> companies = CompanyDao.listAll(con);
            request.setAttribute("companies", companies);
            String editId = request.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                try {
                    Company edit = CompanyDao.findById(con, Long.parseLong(editId));
                    request.setAttribute("editCompany", edit);
                } catch (NumberFormatException ex) {
                    LOG.log(Level.WARNING, "Bad company edit id", ex);
                }
            }
            request.getRequestDispatcher("/admin/companies.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin companies list failed", ex);
            request.setAttribute("pageError", "Companies unavailable. Please try again later.");
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
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            handleDelete(request, response, session);
        } else if ("add".equals(action) || "update".equals(action)) {
            handleSave(request, response, session, "update".equals(action));
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, boolean isUpdate) throws IOException {
        Map<String, String> errors = new LinkedHashMap<>();
        long companyId = -1;
        if (isUpdate) {
            try {
                companyId = Long.parseLong(request.getParameter("companyId"));
            } catch (NumberFormatException ex) {
                session.setAttribute("flashError", "Invalid company.");
                response.sendRedirect(companyUrl(request));
                return;
            }
        }

        String name = clean(request.getParameter("companyName"));
        String jobRole = clean(request.getParameter("jobRole"));
        String branches = clean(request.getParameter("eligibleBranches"));
        String description = clean(request.getParameter("description"));
        BigDecimal pkg = parseDecimal(request.getParameter("packageLpa"), "package", errors, false);
        BigDecimal minCgpa = parseDecimal(request.getParameter("minimumCgpa"), "minimum CGPA", errors, true);
        Date deadline = parseDate(request.getParameter("applicationDeadline"), errors);

        if (name.isEmpty() || name.length() > 100) {
            errors.put("companyName", "Company name is required (max 100).");
        }
        if (jobRole.isEmpty() || jobRole.length() > 100) {
            errors.put("jobRole", "Job role is required (max 100).");
        }
        if (branches.length() > 255) {
            errors.put("eligibleBranches", "Eligible branches too long (max 255).");
        }
        if (description.length() > 2000) {
            errors.put("description", "Description too long (max 2000).");
        }
        if (minCgpa != null && (minCgpa.compareTo(BigDecimal.ZERO) < 0
                || minCgpa.compareTo(new BigDecimal("10")) > 0)) {
            errors.put("minimumCgpa", "Minimum CGPA must be between 0 and 10.");
        }
        if (pkg != null && pkg.compareTo(BigDecimal.ZERO) < 0) {
            errors.put("packageLpa", "Package must not be negative.");
        }

        if (!errors.isEmpty()) {
            session.setAttribute("flashError", String.join(" ", errors.values()));
            response.sendRedirect(companyUrl(request));
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (isUpdate) {
                int rows = CompanyDao.update(con, companyId, name, jobRole, pkg, minCgpa,
                        branches.isEmpty() ? null : branches, deadline,
                        description.isEmpty() ? null : description);
                session.setAttribute(rows == 1 ? "flashSuccess" : "flashError",
                        rows == 1 ? "Company updated." : "Company not found.");
            } else {
                CompanyDao.insert(con, name, jobRole, pkg, minCgpa,
                        branches.isEmpty() ? null : branches, deadline,
                        description.isEmpty() ? null : description);
                session.setAttribute("flashSuccess", "Company added.");
            }
            response.sendRedirect(companyUrl(request));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Company save failed", ex);
            session.setAttribute("flashError", "Save failed due to a server error.");
            response.sendRedirect(companyUrl(request));
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws IOException {
        long companyId;
        try {
            companyId = Long.parseLong(request.getParameter("companyId"));
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError", "Invalid company.");
            response.sendRedirect(companyUrl(request));
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            long refs = CompanyDao.countApplications(con, companyId);
            if (refs > 0) {
                session.setAttribute("flashError",
                        "Cannot delete: " + refs + " application(s) reference this company.");
                response.sendRedirect(companyUrl(request));
                return;
            }
            int rows = CompanyDao.delete(con, companyId);
            session.setAttribute(rows == 1 ? "flashSuccess" : "flashError",
                    rows == 1 ? "Company deleted." : "Company not found.");
            response.sendRedirect(companyUrl(request));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Company delete failed", ex);
            session.setAttribute("flashError", "Delete failed due to a server error.");
            response.sendRedirect(companyUrl(request));
        }
    }

    private static String companyUrl(HttpServletRequest request) {
        return request.getContextPath() + "/admin/companies";
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private static BigDecimal parseDecimal(String raw, String field,
            Map<String, String> errors, boolean cgpaRange) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            BigDecimal v = new BigDecimal(raw.trim());
            if (!Double.isFinite(v.doubleValue())) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException ex) {
            errors.put(field, "Invalid " + field + " number.");
            return null;
        }
    }

    private static Date parseDate(String raw, Map<String, String> errors) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(LocalDate.parse(raw.trim()));
        } catch (Exception ex) {
            errors.put("deadline", "Invalid deadline date (use yyyy-MM-dd).");
            return null;
        }
    }
}
