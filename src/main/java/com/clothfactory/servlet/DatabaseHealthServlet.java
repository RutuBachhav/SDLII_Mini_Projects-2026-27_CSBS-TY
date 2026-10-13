package com.clothfactory.servlet;

import com.clothfactory.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/health")
public class DatabaseHealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection connection = DBConnection.getConnection()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().printf("{\"status\":\"UP\",\"database\":\"%s\"}%n", connection.getMetaData().getDatabaseProductName());
        } catch (SQLException exception) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().print("{\"status\":\"DOWN\",\"database\":\"unavailable\"}");
            getServletContext().log("Database health check failed", exception);
        }
    }
}