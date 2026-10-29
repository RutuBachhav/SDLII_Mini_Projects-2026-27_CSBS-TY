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

@WebServlet("/TrackServlet")
public class TrackServlet extends HttpServlet {

    private static final String URL =
            "jdbc:mysql://localhost:3306/courier_bay?sslMode=DISABLED";

    private static final String USER = "root";

    // Enter the same MySQL password used in BookingServlet.java.
    private static final String PASSWORD = "Pass@123";

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String trackingId = request.getParameter("tracking_id");

        try (PrintWriter out = response.getWriter()) {

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

                String sql = "SELECT * FROM couriers WHERE tracking_id = ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, trackingId);

                    try (ResultSet rs = ps.executeQuery()) {

                        out.println("<html><head><title>Courier Tracking</title>");
                        out.println("<style>");
                        out.println("body{font-family:Arial;background:#f4f7fb;text-align:center;padding:40px;}");
                        out.println(".box{background:white;padding:30px;margin:auto;max-width:550px;border-radius:12px;}");
                        out.println("h1{color:#102a56;} p{font-size:17px;color:#334155;}");
                        out.println("a{color:#1765a8;text-decoration:none;font-weight:bold;}");
                        out.println("</style></head><body><div class='box'>");

                        if (rs.next()) {

                            out.println("<h1>Courier Details</h1>");
                            out.println("<p><b>Tracking ID:</b> " + rs.getString("tracking_id") + "</p>");
                            out.println("<p><b>Sender:</b> " + rs.getString("sender_name") + "</p>");
                            out.println("<p><b>Receiver:</b> " + rs.getString("receiver_name") + "</p>");
                            out.println("<p><b>Courier Type:</b> " + rs.getString("courier_type") + "</p>");
                            out.println("<p><b>Weight:</b> " + rs.getString("weight") + " kg</p>");
                            out.println("<p><b>Booking Date:</b> " + rs.getString("booking_date") + "</p>");
                            out.println("<h2>Status: " + rs.getString("delivery_status") + "</h2>");

                        } else {
                            out.println("<h1>Tracking ID Not Found</h1>");
                            out.println("<p>Please check your tracking ID and try again.</p>");
                        }

                        out.println("<p><a href='track.html'>Track Another Courier</a></p>");
                        out.println("<p><a href='index.html'>Back to Home</a></p>");
                        out.println("</div></body></html>");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Tracking Error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("<h2>Tracking Failed</h2>");
                out.println("<p>Please check the server configuration.</p>");
            }
        }
    }
}