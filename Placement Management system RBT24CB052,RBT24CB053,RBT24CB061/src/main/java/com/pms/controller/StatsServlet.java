package com.pms.controller;

import com.pms.dao.StatsDao;
import com.pms.model.DashboardStats;
import com.pms.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Public JSON counters for the homepage live stats. Never exposes secrets. */
@WebServlet("/api/stats")
public class StatsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StatsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try (Connection con = DBConnection.getConnection()) {
            DashboardStats s = StatsDao.dashboard(con);
            long rate = s.getApplications() == 0 ? 0
                    : Math.round(s.getSelected() * 100.0 / s.getApplications());
            response.getWriter().printf(
                    "{\"students\":%d,\"companies\":%d,\"applications\":%d,"
                    + "\"selected\":%d,\"placementRate\":%d}",
                    s.getStudents(), s.getCompanies(), s.getApplications(), s.getSelected(), rate);
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Public stats unavailable", ex);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"unavailable\":true}");
        }
    }
}
