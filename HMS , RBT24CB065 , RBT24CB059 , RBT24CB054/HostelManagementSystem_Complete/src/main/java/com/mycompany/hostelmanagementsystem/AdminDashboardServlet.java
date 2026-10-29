package com.mycompany.hostelmanagementsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/AdminDashboardServlet"})
public class AdminDashboardServlet extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("studentCount", dashboardDAO.countStudents());
        request.setAttribute("roomCount", dashboardDAO.countRooms());
        request.setAttribute("availableRoomCount", dashboardDAO.countAvailableRooms());
        request.setAttribute("pendingFeeCount", dashboardDAO.countPendingFees());
        request.setAttribute("openComplaintCount", dashboardDAO.countOpenComplaints());

        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }
}
