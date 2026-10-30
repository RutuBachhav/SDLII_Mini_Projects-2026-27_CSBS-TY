
package com.studentplanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/tasks")
public class TaskServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String taskName = request.getParameter("taskName");
        String subject = request.getParameter("subject");
        String deadline = request.getParameter("deadline");
        String priority = request.getParameter("priority");

        String sql = "INSERT INTO tasks "
                + "(task_name, subject, deadline, priority, status) "
                + "VALUES (?, ?, ?, ?, 'Pending')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, taskName);
            ps.setString(2, subject);
            ps.setDate(3, java.sql.Date.valueOf(deadline));
            ps.setString(4, priority);

            ps.executeUpdate();

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Task saved successfully!");

        } catch (Exception e) {
            throw new ServletException("Unable to save task.", e);
        }
    }
}
