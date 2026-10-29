
package com.courierbay.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.UUID;

@WebServlet("/BookingServlet")
public class BookingServlet extends HttpServlet {

  private static final String URL =
        "jdbc:mysql://localhost:3306/courier_bay?sslMode=DISABLED";

    private static final String USER = "root";

    // Enter your MySQL root password here.
    private static final String PASSWORD = "Pass@123";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
      
       System.out.println("BookingServlet is working!");
        String senderName = request.getParameter("sender_name");
        String senderPhone = request.getParameter("sender_phone");
        String receiverName = request.getParameter("receiver_name");
        String receiverPhone = request.getParameter("receiver_phone");
        String pickupAddress = request.getParameter("pickup_address");
        String deliveryAddress = request.getParameter("delivery_address");
        String courierType = request.getParameter("courier_type");
        String weightText = request.getParameter("weight");

        try (PrintWriter out = response.getWriter()) {

            double weight = Double.parseDouble(weightText);

            String trackingId = "CB" +
                    UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 10).toUpperCase();

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Step 2: MySQL driver loaded");

            String sql = "INSERT INTO couriers "
                    + "(tracking_id, sender_name, sender_phone, "
                    + "receiver_name, receiver_phone, pickup_address, "
                    + "delivery_address, courier_type, weight) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = DriverManager.getConnection(
                        URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, trackingId);
                ps.setString(2, senderName);
                ps.setString(3, senderPhone);
                ps.setString(4, receiverName);
                ps.setString(5, receiverPhone);
                ps.setString(6, pickupAddress);
                ps.setString(7, deliveryAddress);
                ps.setString(8, courierType);
                ps.setDouble(9, weight);

                ps.executeUpdate();
                System.out.println("Step 3: Data inserted into MySQL");

                out.println("<html><body style='font-family:Arial;"
                        + "text-align:center;padding:60px;"
                        + "background:#f4f7fb;'>");

                out.println("<h1 style='color:#1765a8;'>"
                        + "Courier Booked Successfully!</h1>");

                out.println("<h2>Your Tracking ID:</h2>");

                out.println("<h1 style='color:#102a56;'>"
                        + trackingId + "</h1>");

                out.println("<p>Please save this tracking ID "
                        + "to track your courier.</p>");

                out.println("<a href='index.html'>Back to Home</a>");

                out.println("</body></html>");
            }

        } catch (Exception e) {

            
           System.out.println("ERROR: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try (PrintWriter out = response.getWriter()) {
                System.out.println("Step 1: Servlet started");
                out.println("<h2>Courier Booking Failed</h2>");
                out.println("<p>Please check the server configuration.</p>");
                out.println("<p>" + e.getMessage() + "</p>");
            }
        }
    }
}
