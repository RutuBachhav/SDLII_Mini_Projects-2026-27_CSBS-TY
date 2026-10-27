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

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

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

            String search = request.getParameter("search");
            String category = request.getParameter("category");
            String status = request.getParameter("status");

            StringBuilder jpql = new StringBuilder(
                    "SELECT f FROM FoodItem f WHERE 1=1"
            );

            if (search != null && !search.trim().isEmpty()) {
                jpql.append(
                    " AND LOWER(f.foodName) LIKE LOWER(:search)"
                );
            }

            if (category != null
                    && !category.trim().isEmpty()
                    && !category.equals("ALL")) {

                jpql.append(
                    " AND f.category = :category"
                );
            }

            if (status != null
                    && !status.trim().isEmpty()
                    && !status.equals("ALL")) {

                if (status.equals("LOW")) {
                    jpql.append(
                        " AND f.quantity <= f.minimumStock"
                    );
                }

                if (status.equals("AVAILABLE")) {
                    jpql.append(
                        " AND f.quantity > f.minimumStock"
                    );
                }
            }

            jpql.append(" ORDER BY f.id");

            var query = em.createQuery(
                    jpql.toString(),
                    FoodItem.class
            );

            if (search != null && !search.trim().isEmpty()) {
                query.setParameter(
                    "search",
                    "%" + search.trim() + "%"
                );
            }

            if (category != null
                    && !category.trim().isEmpty()
                    && !category.equals("ALL")) {

                query.setParameter("category", category);
            }

            List<FoodItem> foodList = query.getResultList();

            request.setAttribute("foodList", foodList);
            request.setAttribute("search", search);
            request.setAttribute("category", category);
            request.setAttribute("status", status);

            request.getRequestDispatcher("inventory.jsp")
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