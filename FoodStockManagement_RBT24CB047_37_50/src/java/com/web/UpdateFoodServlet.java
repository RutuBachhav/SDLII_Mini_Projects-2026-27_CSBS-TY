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

@WebServlet("/updateFood")
public class UpdateFoodServlet extends HttpServlet {

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

            Long id = Long.parseLong(
                    request.getParameter("id")
            );

            FoodItem food = em.find(FoodItem.class, id);

            if (food == null) {

                response.getWriter().println(
                        "Food item not found."
                );

                return;
            }

            food.setFoodName(
                    request.getParameter("foodName")
            );

            food.setCategory(
                    request.getParameter("category")
            );

            food.setQuantity(
                    Integer.parseInt(
                            request.getParameter("quantity")
                    )
            );

            food.setUnit(
                    request.getParameter("unit")
            );

            food.setPrice(
                    Double.parseDouble(
                            request.getParameter("price")
                    )
            );

            food.setMinimumStock(
                    Integer.parseInt(
                            request.getParameter("minimumStock")
                    )
            );

            food.setSupplier(
                    request.getParameter("supplier")
            );

            food.setExpiryDate(
                    LocalDate.parse(
                            request.getParameter("expiryDate")
                    )
            );

            em.getTransaction().begin();

            em.merge(food);

            em.getTransaction().commit();

            response.sendRedirect("inventory");

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Error Updating Food Item</h2>"
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