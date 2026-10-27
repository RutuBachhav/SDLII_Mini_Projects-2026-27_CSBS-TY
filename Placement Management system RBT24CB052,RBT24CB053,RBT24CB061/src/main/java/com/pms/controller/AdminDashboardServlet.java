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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** GET /admin/dashboard — live counts, status breakdown, recent applications. */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminDashboardServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection con = DBConnection.getConnection()) {
            DashboardStats stats = StatsDao.dashboard(con);
            Map<String, Long> byStatus = StatsDao.applicationsByStatus(con);
            List<ApplicationView> recent = ApplicationDao.listAll(con, null, null);
            request.setAttribute("stats", stats);
            request.setAttribute("byStatus", byStatus);
            request.setAttribute("recent", recent.size() > 10 ? recent.subList(0, 10) : recent);
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Admin dashboard failed", ex);
            request.setAttribute("pageError", "Dashboard unavailable. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
