package com.web;

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

@WebServlet("/transactionHistory")
public class TransactionHistoryServlet extends HttpServlet {

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

            List<StockTransaction> transactionList =
                    em.createQuery(
                            "SELECT s FROM StockTransaction s "
                            + "ORDER BY s.id DESC",
                            StockTransaction.class
                    ).getResultList();

            request.setAttribute(
                    "transactionList",
                    transactionList
            );

            request.getRequestDispatcher(
                    "transactionHistory.jsp"
            ).forward(request, response);

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