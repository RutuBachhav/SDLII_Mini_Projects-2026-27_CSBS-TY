package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(name = "StudentServlet", urlPatterns = {"/StudentServlet"})
public class StudentServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

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
            if ("new".equals(action)) {
                request.setAttribute("availableRooms", studentDAO.getAvailableRooms());
                request.getRequestDispatcher("/admin/student-form.jsp").forward(request, response);
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                studentDAO.deleteStudent(id);
                response.sendRedirect(request.getContextPath() + "/StudentServlet");
            } else {
                request.setAttribute("students", studentDAO.getAllStudents());
                request.getRequestDispatcher("/admin/students.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("students", studentDAO.getAllStudents());
            request.getRequestDispatcher("/admin/students.jsp").forward(request, response);
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
            String name = request.getParameter("name");
            String contact = request.getParameter("contact");
            String guardianContact = request.getParameter("guardianContact");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            Integer roomId = Integer.parseInt(request.getParameter("roomId"));

            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Student name is required.");
            }

            Student student = new Student();
            student.setName(name.trim());
            student.setContact(contact);
            student.setGuardianContact(guardianContact);
            student.setAdmissionDate(LocalDate.now());

            studentDAO.admitStudent(student, username, password, roomId);

            response.sendRedirect(request.getContextPath() + "/StudentServlet");

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("availableRooms", studentDAO.getAvailableRooms());
            request.getRequestDispatcher("/admin/student-form.jsp").forward(request, response);
        }
    }
}
