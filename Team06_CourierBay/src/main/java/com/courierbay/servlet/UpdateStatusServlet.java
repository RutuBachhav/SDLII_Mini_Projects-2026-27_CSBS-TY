
package com.courierbay.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UpdateStatusServlet")
public class UpdateStatusServlet extends HttpServlet {

    private static final String URL =
            "jdbc:mysql://localhost:3306/courier_bay?sslMode=DISABLED";

    private static final String USER = "root";

    // Use the same MySQL password as BookingServlet.java.
    private static final String PASSWORD = "Pass@123";

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        String trackingId = request.getParameter("tracking_id");
        String status = request.getParameter("delivery_status");

        String[] allowedStatuses = {
            "Booked",
            "In Transit",
            "Out for Delivery",
            "Delivered"
        };

        boolean validStatus = false;

        for (String allowed : allowedStatuses) {
            if (allowed.equals(status)) {
                validStatus = true;
                break;
            }
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            if (trackingId == null || trackingId.trim().isEmpty()
                    || !validStatus) {

                out.println("<h2>Invalid tracking ID or delivery status.</h2>");
                out.println("<a href='ViewCouriersServlet'>Go Back</a>");
                return;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD)) {

                String sql =
                        "UPDATE couriers SET delivery_status = ? WHERE tracking_id = ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, status);
                    ps.setString(2, trackingId.trim());

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        out.println("<h2>Delivery Status Updated Successfully!</h2>");
                        out.println("<p>Tracking ID: " + trackingId + "</p>");
                        out.println("<p>New Status: " + status + "</p>");
                    } else {
                        out.println("<h2>Tracking ID Not Found!</h2>");
                    }

                    out.println("<a href='ViewCouriersServlet'>Back to All Couriers</a>");
                }
            }

        } catch (Exception e) {

            System.out.println("Update Status Error: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try (PrintWriter out = response.getWriter()) {
                out.println("<h2>Unable to update delivery status.</h2>");
                out.println("<p>Please check the server configuration.</p>");
            }
        }
    }
}