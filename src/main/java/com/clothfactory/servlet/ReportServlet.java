package com.clothfactory.servlet;

import com.clothfactory.dao.DashboardDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("inventory", dashboardDAO.findInventory());
            request.setAttribute("lowStock", dashboardDAO.findLowStock());
            request.getRequestDispatcher("/WEB-INF/views/reports/inventory.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load reports", exception);
        }
    }
}