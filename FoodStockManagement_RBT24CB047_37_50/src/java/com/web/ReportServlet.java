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
import java.util.List;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {

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

            List<FoodItem> foodList = em.createQuery(
                    "SELECT f FROM FoodItem f ORDER BY f.foodName",
                    FoodItem.class
            ).getResultList();

            Number totalItems = em.createQuery(
                    "SELECT COUNT(f) FROM FoodItem f",
                    Number.class
            ).getSingleResult();

            Number totalQuantity = em.createQuery(
                    "SELECT COALESCE(SUM(f.quantity), 0) "
                    + "FROM FoodItem f",
                    Number.class
            ).getSingleResult();

            Number lowStockItems = em.createQuery(
                    "SELECT COUNT(f) FROM FoodItem f "
                    + "WHERE f.quantity <= f.minimumStock",
                    Number.class
            ).getSingleResult();

            Number stockValue = em.createQuery(
                    "SELECT COALESCE(SUM(f.quantity * f.price), 0) "
                    + "FROM FoodItem f",
                    Number.class
            ).getSingleResult();

            request.setAttribute(
                    "foodList",
                    foodList
            );

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
                    "stockValue",
                    stockValue.doubleValue()
            );

            request.getRequestDispatcher("reports.jsp")
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