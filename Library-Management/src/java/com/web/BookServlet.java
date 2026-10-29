package com.web;

import com.entity.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "BookServlet", urlPatterns = {"/saveBook", "/BookAddServlet"})
public class BookServlet extends HttpServlet {

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
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String isbn = request.getParameter("isbn");
            String category = request.getParameter("category");
            String publisher = request.getParameter("publisher");
            String pubYearStr = request.getParameter("publicationYear");
            String totalCopiesStr = request.getParameter("totalCopies");

            if (title == null || title.trim().isEmpty() || author == null || author.trim().isEmpty()) {
                response.getWriter().println("<h2 style='color:red'>Title and Author are required!</h2>");
                response.getWriter().println("<br><a href='addBook.html'>Try Again</a>");
                return;
            }

            int publicationYear = (pubYearStr != null && !pubYearStr.trim().isEmpty()) ? Integer.parseInt(pubYearStr.trim()) : 2024;
            int totalCopies = (totalCopiesStr != null && !totalCopiesStr.trim().isEmpty()) ? Integer.parseInt(totalCopiesStr.trim()) : 1;

            Book book = new Book();
            book.setTitle(title.trim());
            book.setAuthor(author.trim());
            book.setIsbn(isbn != null ? isbn.trim() : "");
            book.setCategory(category != null ? category.trim() : "");
            book.setPublisher(publisher != null ? publisher.trim() : "");
            book.setPublicationYear(publicationYear);
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(totalCopies);

            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(book);
            em.getTransaction().commit();

            response.getWriter().println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Book Saved</title>");
            response.getWriter().println("<style>body{font-family:'Inter',Arial,sans-serif;background:#f0f4f8;padding:40px;display:flex;justify-content:center;align-items:center;min-height:80vh;}");
            response.getWriter().println(".card{background:white;padding:36px;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);max-width:480px;width:100%;text-align:center;}");
            response.getWriter().println("h2{color:#27ae60;margin-bottom:16px;}p{color:#4a5568;margin:8px 0;font-size:15px;}");
            response.getWriter().println("a{display:inline-block;margin:8px;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;}");
            response.getWriter().println(".btn-primary{background:#3498db;color:white;}.btn-home{background:#e2e8f0;color:#2d3748;}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='card'>");
            response.getWriter().println("<h2>&#10003; Book Saved Successfully!</h2>");
            response.getWriter().println("<p><strong>Book ID:</strong> " + book.getBookId() + "</p>");
            response.getWriter().println("<p><strong>Title:</strong> " + book.getTitle() + "</p>");
            response.getWriter().println("<p><strong>Author:</strong> " + book.getAuthor() + "</p>");
            response.getWriter().println("<p><strong>Copies:</strong> " + book.getTotalCopies() + "</p>");
            response.getWriter().println("<br>");
            response.getWriter().println("<a href='viewBook.jsp' class='btn-primary'>View Books</a>");
            response.getWriter().println("<a href='addBook.html' class='btn-primary'>Add Another</a>");
            response.getWriter().println("<a href='index.html' class='btn-home'>Back to Home</a>");
            response.getWriter().println("</div></body></html>");

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            response.getWriter().println("<h2 style='color:red'>Error: " + e.getMessage() + "</h2>");
            response.getWriter().println("<br><a href='addBook.html'>Back to Form</a>");
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
