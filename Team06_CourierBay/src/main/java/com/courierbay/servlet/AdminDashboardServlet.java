
package com.courierbay.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AdminDashboardServlet")
public class AdminDashboardServlet extends HttpServlet {

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

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Courier Bay Admin Dashboard</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box;font-family:Arial,sans-serif;}");
            out.println("body{margin:0;background:#f4f7fb;color:#172554;}");
            out.println("header{background:#102a56;color:white;padding:22px 8%;display:flex;justify-content:space-between;align-items:center;}");
            out.println(".logo{font-size:25px;font-weight:bold;}");
            out.println(".logo span{color:#38bdf8;}");
            out.println(".container{max-width:1000px;margin:70px auto;padding:35px;background:white;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.08);}");
            out.println("h1{color:#102a56;}");
            out.println(".card{background:#eaf4ff;padding:25px;border-radius:10px;margin-top:25px;}");
            out.println(".card h2{color:#1765a8;}");
            out.println("a{display:inline-block;background:#1765a8;color:white;padding:12px 20px;text-decoration:none;border-radius:6px;margin:10px 10px 0 0;}");
            out.println("a:hover{background:#102a56;}");
            out.println("footer{text-align:center;padding:20px;background:#102a56;color:white;margin-top:60px;}");
            out.println("</style></head><body>");

            out.println("<header>");
            out.println("<div class='logo'>COURIER <span>BAY</span></div>");
            out.println("<div>Admin Panel</div>");
            out.println("</header>");

            out.println("<div class='container'>");
            out.println("<h1>Welcome, Admin!</h1>");
            out.println("<p>Manage your Courier Bay operations from here.</p>");

            out.println("<div class='card'>");
            out.println("<h2>Courier Management</h2>");
            out.println("<p>View and manage all courier bookings.</p>");
            out.println("<a href='ViewCouriersServlet'>View All Couriers</a>");
            out.println("</div>");

            out.println("<div class='card'>");
            out.println("<h2>Shipment Tracking</h2>");
            out.println("<p>Check the status of customer shipments.</p>");
            out.println("<a href='track.html'>Track Courier</a>");
            out.println("</div>");

            out.println("<a href='AdminLogoutServlet'>Logout</a>");
            out.println("</div>");

            out.println("<footer>© 2026 Courier Bay | Admin Dashboard</footer>");

            out.println("</body></html>");
        }
    }
}