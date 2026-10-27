package com.web;

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

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

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

            // Total food items
            Number totalItems = em.createQuery(
                    "SELECT COUNT(f) FROM FoodItem f",
                    Number.class
            ).getSingleResult();

            // Total stock quantity
            Number totalQuantity = em.createQuery(
                    "SELECT COALESCE(SUM(f.quantity), 0) FROM FoodItem f",
                    Number.class
            ).getSingleResult();

            // Low stock items
            Number lowStockItems = em.createQuery(
                    "SELECT COUNT(f) FROM FoodItem f "
                    + "WHERE f.quantity <= f.minimumStock",
                    Number.class
            ).getSingleResult();
            
            // Expiring within 7 days
LocalDate today = LocalDate.now();
LocalDate sevenDaysLater = today.plusDays(7);

Number expiringSoon = em.createQuery(
        "SELECT COUNT(f) FROM FoodItem f "
        + "WHERE f.expiryDate IS NOT NULL "
        + "AND f.expiryDate >= :today "
        + "AND f.expiryDate <= :sevenDaysLater",
        Number.class
)
.setParameter("today", today)
.setParameter("sevenDaysLater", sevenDaysLater)
.getSingleResult();

            request.setAttribute(
                    "totalItems",
                    totalItems.intValue()
            );

            request.setAttribute(
                    "totalQuantity",
                    totalQuantity.intValue()
            );

            request.setAttribute(
                    "lowStockItems",
                    lowStockItems.intValue()
            );
            
            request.setAttribute(
        "expiringSoon",
        expiringSoon.intValue()
);

            request.getRequestDispatcher("dashboard.jsp")
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