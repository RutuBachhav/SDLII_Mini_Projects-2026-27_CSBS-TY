package com.pms.controller;

import com.pms.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests MySQL connectivity to placement_db.
 * Mapped to /test-db. Displays only a generic success/failure message;
 * technical details are logged server-side.
 */
@WebServlet("/test-db")
public class DatabaseTestServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DatabaseTestServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        boolean success = false;
        try (Connection con = DBConnection.getConnection()) {
            success = (con != null && con.isValid(2));
        } catch (SQLException ex) {
            // Server-side only: full detail goes to GlassFish server.log, never to the browser.
            LOG.log(Level.SEVERE, "Database connection test failed", ex);
            success = false;
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\"><head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<title>Database Test | Placement Management System</title>");
            out.println("<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">");
            out.println("</head><body class=\"bg-light\">");
            out.println("<div class=\"container py-5\"><div class=\"row justify-content-center\"><div class=\"col-md-6\">");
            out.println("<div class=\"card shadow border-0\"><div class=\"card-body p-4 text-center\">");
            if (success) {
                out.println("<h3 class=\"fw-bold text-success\">Database connection successful.</h3>");
                out.println("<p class=\"text-muted\">Connected to placement_db.</p>");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("<h3 class=\"fw-bold text-danger\">Database connection failed.</h3>");
                out.println("<p class=\"text-muted\">Please contact the administrator.</p>");
            }
            out.println("<a href=\"" + request.getContextPath() + "/\" class=\"btn btn-primary mt-2\">Back to Home</a>");
            out.println("</div></div></div></div></div>");
            out.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
