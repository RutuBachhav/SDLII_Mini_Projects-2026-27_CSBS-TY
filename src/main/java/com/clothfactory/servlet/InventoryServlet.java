package com.clothfactory.servlet;

import com.clothfactory.dao.InventoryDAO;
import com.clothfactory.dao.ProductDAO;
import com.clothfactory.model.Product;
import com.clothfactory.model.StockTransaction;
import com.clothfactory.service.InsufficientStockException;
import com.clothfactory.service.InventoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/inventory/*")
public class InventoryServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final InventoryService inventoryService = new InventoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if (path == null || "/".equals(path)) {
                request.setAttribute("transactions", inventoryDAO.findRecent(100));
                request.getRequestDispatcher("/WEB-INF/views/inventory/list.jsp").forward(request, response);
            } else if ("/new".equals(path)) {
                showForm(request, response, parseType(request.getParameter("type")));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException exception) {
            throw new ServletException("Unable to read inventory", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!"/move".equals(request.getPathInfo())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            StockTransaction.Type type = parseType(request.getParameter("type"));
            long productId = Long.parseLong(request.getParameter("productId"));
            BigDecimal quantity = new BigDecimal(request.getParameter("quantity"));
            String reason = request.getParameter("reason");
            inventoryService.recordMovement(productId, type, quantity, reason);
            response.sendRedirect(request.getContextPath() + "/inventory");
        } catch (InsufficientStockException exception) {
            showFormWithError(request, response, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            showFormWithError(request, response, "Enter a valid product, positive quantity, and reason.");
        } catch (SQLException exception) {
            throw new ServletException("Unable to record stock movement", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, StockTransaction.Type type) throws SQLException, ServletException, IOException {
        request.setAttribute("products", productDAO.findAll(null, null));
        request.setAttribute("type", type);
        request.getRequestDispatcher("/WEB-INF/views/inventory/form.jsp").forward(request, response);
    }

    private void showFormWithError(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException {
        try {
            request.setAttribute("products", productDAO.findAll(null, null));
        } catch (SQLException exception) {
            throw new ServletException("Unable to reload products", exception);
        }
        request.setAttribute("type", parseType(request.getParameter("type")));
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/inventory/form.jsp").forward(request, response);
    }

    private static StockTransaction.Type parseType(String value) {
        try {
            return StockTransaction.Type.valueOf(value == null ? "STOCK_IN" : value);
        } catch (IllegalArgumentException exception) {
            return StockTransaction.Type.STOCK_IN;
        }
    }
}