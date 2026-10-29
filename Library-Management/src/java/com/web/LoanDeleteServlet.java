package com.web;

import com.entity.Book;
import com.entity.Loan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LoanDeleteServlet", urlPatterns = {"/deleteLoan", "/LoanDeleteServlet"})
public class LoanDeleteServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = com.persistence.PersistenceManager.createEntityManagerFactory();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        EntityManager em = null;

        try {
            int loanId = Integer.parseInt(request.getParameter("loanId"));

            em = emf.createEntityManager();
            em.getTransaction().begin();

            Loan loan = em.find(Loan.class, loanId);

            if (loan == null) {
                em.getTransaction().rollback();
                response.getWriter().println("<h2 style='color:red'>Loan Record Not Found</h2>");
                response.getWriter().println("<br><a href='viewLoan.jsp'>View Loans</a>");
                return;
            }

            // If loan is still issued, restore book inventory copy
            if (!loan.isReturned()) {
                Book book = loan.getBook();
                if (book != null) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    em.merge(book);
                }
            }

            em.remove(loan);
            em.getTransaction().commit();

            response.getWriter().println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Loan Deleted</title>");
            response.getWriter().println("<style>body{font-family:'Inter',Arial,sans-serif;background:#f0f4f8;padding:40px;display:flex;justify-content:center;align-items:center;min-height:80vh;}");
            response.getWriter().println(".card{background:white;padding:36px;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);max-width:480px;width:100%;text-align:center;}");
            response.getWriter().println("h2{color:#27ae60;margin-bottom:16px;}p{color:#4a5568;margin:8px 0;font-size:15px;}");
            response.getWriter().println("a{display:inline-block;margin:8px;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;}");
            response.getWriter().println(".btn-primary{background:#3498db;color:white;}.btn-home{background:#e2e8f0;color:#2d3748;}");
            response.getWriter().println("</style></head><body><div class='card'>");
            response.getWriter().println("<h2>&#10003; Loan Record Deleted Successfully</h2>");
            response.getWriter().println("<p>Deleted Loan ID: " + loanId + "</p>");
            response.getWriter().println("<br>");
            response.getWriter().println("<a href='viewLoan.jsp' class='btn-primary'>View Loans</a>");
            response.getWriter().println("<a href='index.html' class='btn-home'>Back to Home</a>");
            response.getWriter().println("</div></body></html>");

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            response.getWriter().println("<h2 style='color:red'>Error: " + e.getMessage() + "</h2>");
            response.getWriter().println("<br><a href='deleteLoan.jsp'>Back</a>");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
