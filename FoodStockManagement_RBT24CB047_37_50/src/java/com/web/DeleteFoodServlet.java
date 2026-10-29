package com.web;

import com.entity.FoodItem;
import com.entity.StockTransaction;

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

@WebServlet("/deleteFood")
public class DeleteFoodServlet extends HttpServlet {

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

            Long id = Long.parseLong(
                    request.getParameter("id")
            );

            FoodItem food = em.find(FoodItem.class, id);

            if (food != null) {

                em.getTransaction().begin();

                // Find all transactions related to this food item
                List<StockTransaction> transactions =
                        em.createQuery(
                                "SELECT s FROM StockTransaction s "
                                + "WHERE s.foodItem.id = :foodId",
                                StockTransaction.class
                        )
                        .setParameter("foodId", id)
                        .getResultList();

                // Delete related transactions first
                for (StockTransaction transaction : transactions) {
                    em.remove(transaction);
                }

                // Now delete the food item
                em.remove(food);

                em.getTransaction().commit();
            }

            response.sendRedirect("inventory");

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Error Deleting Food Item</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

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