
package com.courierbay.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ViewCouriersServlet")
public class ViewCouriersServle extends HttpServlet {

    private static final String URL =
            "jdbc:mysql://localhost:3306/courier_bay?sslMode=DISABLED";

    private static final String USER = "root";

    private static final String PASSWORD = "Pass@123";

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD)) {

                String sql = "SELECT * FROM couriers ORDER BY booking_date DESC";

                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    out.println("<!DOCTYPE html>");
                    out.println("<html><head><title>All Couriers</title>");

                    out.println("<style>");
                    out.println("body{font-family:Arial;background:#f4f7fb;margin:0;color:#172554;}");
                    out.println("header{background:#102a56;color:white;padding:22px 5%;}");
                    out.println(".container{padding:30px 5%;}");
                    out.println("h1{color:#102a56;}");
                    out.println("table{width:100%;border-collapse:collapse;background:white;}");
                    out.println("th,td{padding:12px;border:1px solid #ddd;text-align:left;}");
                    out.println("th{background:#1765a8;color:white;}");
                    out.println("tr:nth-child(even){background:#eef4fa;}");
                    out.println("a{display:inline-block;margin-top:20px;background:#1765a8;color:white;padding:12px 20px;text-decoration:none;border-radius:5px;}");
                    out.println("select{padding:8px;border:1px solid #ccc;border-radius:5px;}");
                    out.println("button{padding:9px 14px;background:#1765a8;color:white;border:none;border-radius:5px;cursor:pointer;}");
                    out.println("</style></head><body>");

                    out.println("<header><h2>COURIER BAY - Admin Panel</h2></header>");
                    out.println("<div class='container'>");
                    out.println("<h1>All Courier Bookings</h1>");

                    out.println("<table>");
                    out.println("<tr>");
                    out.println("<th>ID</th>");
                    out.println("<th>Tracking ID</th>");
                    out.println("<th>Sender</th>");
                    out.println("<th>Receiver</th>");
                    out.println("<th>Courier Type</th>");
                    out.println("<th>Weight (kg)</th>");
                    out.println("<th>Status</th>");
                    out.println("<th>Update Status</th>");
                    out.println("</tr>");

                    int count = 0;

                    while (rs.next()) {

                        count++;

                        String trackingId = rs.getString("tracking_id");
                        String currentStatus = rs.getString("delivery_status");

                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("courier_id") + "</td>");
                        out.println("<td>" + trackingId + "</td>");
                        out.println("<td>" + rs.getString("sender_name") + "</td>");
                        out.println("<td>" + rs.getString("receiver_name") + "</td>");
                        out.println("<td>" + rs.getString("courier_type") + "</td>");
                        out.println("<td>" + rs.getString("weight") + "</td>");
                        out.println("<td>" + currentStatus + "</td>");

                        out.println("<td>");
                        out.println("<form action='UpdateStatusServlet' method='post'>");

                        out.println("<input type='hidden' name='tracking_id' value='"
                                + trackingId + "'>");

                        out.println("<select name='delivery_status' required>");

                        String[] statuses = {
                            "Booked",
                            "In Transit",
                            "Out for Delivery",
                            "Delivered"
                        };

                        for (String status : statuses) {

                            if (status.equals(currentStatus)) {
                                out.println("<option value='" + status
                                        + "' selected>" + status + "</option>");
                            } else {
                                out.println("<option value='" + status
                                        + "'>" + status + "</option>");
                            }
                        }

                        out.println("</select>");
                        out.println("<button type='submit'>Update</button>");
                        out.println("</form>");
                        out.println("</td>");

                        out.println("</tr>");
                    }

                    out.println("</table>");
                    out.println("<h3>Total Bookings: " + count + "</h3>");
                    out.println("<a href='AdminDashboardServlet'>Back to Dashboard</a>");
                    out.println("</div></body></html>");
                }
            }

        } catch (Exception e) {

            System.out.println("View Couriers Error: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try (PrintWriter out = response.getWriter()) {
                out.println("<h2>Unable to load courier bookings.</h2>");
                out.println("<p>Please check the server configuration.</p>");
            }
        }
    }
}