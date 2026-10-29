package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Complaint;
import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "MyComplaintsServlet", urlPatterns = {"/MyComplaintsServlet"})
public class MyComplaintsServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"STUDENT".equals(request.getSession().getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String username = (String) request.getSession().getAttribute("username");
        Student student = studentDAO.getStudentByUsername(username);

        if (student == null) {
            request.setAttribute("error", "No student record linked to this account.");
            request.getRequestDispatcher("/student/my-complaints.jsp").forward(request, response);
            return;
        }

        List<Complaint> complaints =
                complaintDAO.getComplaintsByStudent(student.getId());

        request.setAttribute("studentId", student.getId());
        request.setAttribute("student", student);
        request.setAttribute("complaints", complaints);

        request.getRequestDispatcher("/student/my-complaints.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"STUDENT".equals(request.getSession().getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String username = (String) request.getSession().getAttribute("username");
            Student student = studentDAO.getStudentByUsername(username);

            if (student == null) {
                throw new RuntimeException("Student record not found.");
            }

            complaintDAO.addComplaint(
                    student.getId(),
                    request.getParameter("description"));

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/MyComplaintsServlet");
    }
}
