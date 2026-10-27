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
import java.time.LocalDate;

@WebServlet("/saveTransaction")
public class SaveTransactionServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("FoodStockPU");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = emf.createEntityManager();

        try {

            Long foodId = Long.parseLong(
                    request.getParameter("foodId")
            );

            String type = request.getParameter("type");

            int quantity = Integer.parseInt(
                    request.getParameter("quantity")
            );

            LocalDate transactionDate =
                    LocalDate.parse(
                            request.getParameter("transactionDate")
                    );

            FoodItem food = em.find(FoodItem.class, foodId);

            if (food == null) {

                response.getWriter().println(
                        "<h2>Food Item Not Found</h2>"
                );

                return;
            }

            // Check stock before STOCK OUT
            if (type.equals("STOCK OUT")
                    && quantity > food.getQuantity()) {

                response.setContentType("text/html");

                response.getWriter().println(
                        "<h2>Insufficient Stock</h2>"
                );

                response.getWriter().println(
                        "<p>Available Stock: "
                        + food.getQuantity()
                        + "</p>"
                );

                response.getWriter().println(
                        "<br><a href='stockTransaction'>Go Back</a>"
                );

                return;
            }

            // Update food quantity
            if (type.equals("STOCK IN")) {

                food.setQuantity(
                        food.getQuantity() + quantity
                );

            } else if (type.equals("STOCK OUT")) {

                food.setQuantity(
                        food.getQuantity() - quantity
                );
            }

            // Create transaction
            StockTransaction transaction =
                    new StockTransaction();

            transaction.setFoodItem(food);
            transaction.setType(type);
            transaction.setQuantity(quantity);
            transaction.setTransactionDate(transactionDate);

            em.getTransaction().begin();

            em.persist(transaction);

            em.getTransaction().commit();

            response.sendRedirect("inventory");

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Error Saving Transaction</h2>"
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