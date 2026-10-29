package com.pms.controller;

import com.pms.dao.ApplicationDao;
import com.pms.dao.StatsDao;
import com.pms.model.ApplicationView;
import com.pms.model.DashboardStats;
import com.pms.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** GET /admin/results — placement results (SELECTED/REJECTED rows) + statistics. */
@WebServlet("/admin/results")
public class AdminResultsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminResultsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection con = DBConnection.getConnection()) {
            List<ApplicationView> all = ApplicationDao.listAll(con, null, null);
            List<ApplicationView> results = new ArrayList<>();
            for (ApplicationView a : all) {
                if ("SELECTED".equals(a.getStatus()) || "REJECTED".equals(a.getStatus())
                        || a.getFinalStatus() != null) {
                    results.add(a);
                }
            }
            DashboardStats stats = StatsDao.dashboard(con);
            Map<String, Long> byStatus = StatsDao.applicationsByStatus(con);
            List<String[]> byCompany = StatsDao.selectionsByCompany(con);
            long placementRate = stats.getApplications() == 0 ? -1
                    : Math.round(stats.getSelected() * 100.0 / stats.getApplications());
            request.setAttribute("results", results);
            request.setAttribute("stats", stats);
            request.setAttribute("placementRate", placementRate);
            request.setAttribute("byStatus", byStatus);
            request.setAttribute("byCompany", byCompany);
            request.getRequestDispatcher("/admin/results.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin results failed", ex);
            request.setAttribute("pageError", "Results unavailable. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
