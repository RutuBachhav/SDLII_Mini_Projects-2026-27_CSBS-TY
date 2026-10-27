package com.web;

import com.entity.FoodItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/expiry")
public class ExpiryServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("FoodStockPU");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = emf.createEntityManager();

        try {

            LocalDate today = LocalDate.now();

            List<FoodItem> expiryList = em.createQuery(
                    "SELECT f FROM FoodItem f "
                    + "WHERE f.expiryDate IS NOT NULL "
                    + "ORDER BY f.expiryDate ASC",
                    FoodItem.class
            ).getResultList();

            request.setAttribute("expiryList", expiryList);
            request.setAttribute("today", today);

            request.getRequestDispatcher("expiry.jsp")
                   .forward(request, response);

        } finally {

            if (em.isOpen()) {
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