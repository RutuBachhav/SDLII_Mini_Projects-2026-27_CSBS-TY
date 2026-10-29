package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.FeePayment;
import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "MyFeesServlet", urlPatterns = {"/MyFeesServlet"})
public class MyFeesServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

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
            request.getRequestDispatcher("/student/my-fees.jsp").forward(request, response);
            return;
        }

        EntityManager em = JPAUtil.createEntityManager();
        try {
            List<FeePayment> fees = em.createQuery(
                    "SELECT f FROM FeePayment f WHERE f.student.id = :studentId ORDER BY f.id DESC",
                    FeePayment.class)
                    .setParameter("studentId", student.getId())
                    .getResultList();

            request.setAttribute("fees", fees);
            request.setAttribute("student", student);
        } finally {
            em.close();
        }

        request.getRequestDispatcher("/student/my-fees.jsp").forward(request, response);
    }
}
