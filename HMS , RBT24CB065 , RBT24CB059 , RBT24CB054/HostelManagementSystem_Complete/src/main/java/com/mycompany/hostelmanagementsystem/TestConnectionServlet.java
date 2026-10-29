package com.mycompany.hostelmanagementsystem;

import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/TestConnectionServlet"})
public class TestConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();

            response.getWriter().println(
                    "<h2>Database connection successful.</h2>");
        } catch (Exception e) {
            response.getWriter().println(
                    "<h2>Database connection failed.</h2><pre>" +
                    escape(e.getMessage()) + "</pre>");
        } finally {
            if (em != null) em.close();
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
