package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "StudentDashboardServlet", urlPatterns = {"/StudentDashboardServlet"})
public class StudentDashboardServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = (String) request.getSession().getAttribute("username");
        Student student = studentDAO.getStudentByUsername(username);

        if (student == null) {
            request.setAttribute("error", "No student record is linked to this account.");
            request.getRequestDispatcher("/student/dashboard.jsp").forward(request, response);
            return;
        }

        EntityManager em = JPAUtil.createEntityManager();
        try {
            Long pendingFees = em.createQuery(
                    "SELECT COUNT(f) FROM FeePayment f WHERE f.student.id = :id AND f.status = 'PENDING'",
                    Long.class)
                    .setParameter("id", student.getId())
                    .getSingleResult();

            Long complaints = em.createQuery(
                    "SELECT COUNT(c) FROM Complaint c WHERE c.student.id = :id AND c.status = 'OPEN'",
                    Long.class)
                    .setParameter("id", student.getId())
                    .getSingleResult();

            request.setAttribute("student", student);
            request.setAttribute("pendingFees", pendingFees);
            request.setAttribute("openComplaints", complaints);
        } finally {
            em.close();
        }

        request.getRequestDispatcher("/student/dashboard.jsp").forward(request, response);
    }
}
