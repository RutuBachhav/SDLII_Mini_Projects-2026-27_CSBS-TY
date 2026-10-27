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

@WebServlet("/editFood")
public class EditFoodServlet extends HttpServlet {

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

            Long id = Long.parseLong(request.getParameter("id"));

            FoodItem food = em.find(FoodItem.class, id);

            if (food != null) {

                request.setAttribute("food", food);

                request.getRequestDispatcher("editFood.jsp")
                       .forward(request, response);

            } else {

                response.getWriter().println("Food item not found.");

            }

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