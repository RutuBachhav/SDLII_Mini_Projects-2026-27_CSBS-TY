package com.mycompany.hostelmanagementsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "ComplaintServlet", urlPatterns = {"/ComplaintServlet"})
public class ComplaintServlet extends HttpServlet {

    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getSession().getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "new":
                    request.setAttribute("students", complaintDAO.getAllStudents());
                    request.getRequestDispatcher("/admin/complaint-form.jsp").forward(request, response);
                    break;

                case "resolve":
                    complaintDAO.resolveComplaint(Integer.parseInt(request.getParameter("id")));
                    response.sendRedirect(request.getContextPath() + "/ComplaintServlet");
                    break;

                case "delete":
                    complaintDAO.deleteComplaint(Integer.parseInt(request.getParameter("id")));
                    response.sendRedirect(request.getContextPath() + "/ComplaintServlet");
                    break;

                default:
                    request.setAttribute("complaints", complaintDAO.getAllComplaints());
                    request.getRequestDispatcher("/admin/complaints.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("complaints", complaintDAO.getAllComplaints());
            request.getRequestDispatcher("/admin/complaints.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            Integer studentId = Integer.parseInt(request.getParameter("studentId"));
            String description = request.getParameter("description");

            complaintDAO.addComplaint(studentId, description);
            response.sendRedirect(request.getContextPath() + "/ComplaintServlet");

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("students", complaintDAO.getAllStudents());
            request.getRequestDispatcher("/admin/complaint-form.jsp").forward(request, response);
        }
    }
}
