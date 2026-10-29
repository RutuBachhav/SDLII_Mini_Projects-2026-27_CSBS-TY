package com.complaint.servlet;

import com.complaint.entity.Complaint;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;

@WebServlet("/ComplaintServlet")
public class ComplaintServlet extends HttpServlet {

    @PersistenceContext(unitName = "ComplaintPU")
    private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String complaintType = request.getParameter("complaintType");
        String description = request.getParameter("description");
        String status = request.getParameter("status");

        System.out.println("Complaint Type: " + complaintType);
        System.out.println("Description: " + description);
        System.out.println("Status: " + status);

        try {
            Complaint complaint = new Complaint();

            complaint.setComplaintType(complaintType);
            complaint.setDescription(description);
            complaint.setStatus(status);

            userTransaction.begin();

            em.persist(complaint);

            userTransaction.commit();

            System.out.println("Complaint saved successfully!");
            System.out.println("Generated ID: " + complaint.getId());

            response.sendRedirect("index.html");

        } catch (Exception e) {
            try {
                if (userTransaction.getStatus()
                        == jakarta.transaction.Status.STATUS_ACTIVE) {
                    userTransaction.rollback();
                }
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();

            throw new ServletException(e);
        }
    }
}

