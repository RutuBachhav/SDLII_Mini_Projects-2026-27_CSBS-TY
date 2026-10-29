package com.clothfactory.servlet;

import com.clothfactory.dao.ProductDAO;
import com.clothfactory.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if (path == null || "/".equals(path)) {
                list(request, response);
            } else if ("/new".equals(path)) {
                request.setAttribute("product", new Product());
                request.getRequestDispatcher("/WEB-INF/views/products/form.jsp").forward(request, response);
            } else if ("/edit".equals(path)) {
                long productId = parseId(request.getParameter("id"));
                Product product = productDAO.findById(productId);
                if (product == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("product", product);
                request.getRequestDispatcher("/WEB-INF/views/products/form.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
        } catch (SQLException exception) {
            throw new ServletException("Unable to read products", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();
        try {
            if ("/save".equals(path)) {
                save(request, response);
            } else if ("/delete".equals(path)) {
                delete(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric value");
        } catch (SQLException exception) {
            throw new ServletException("Unable to save product", exception);
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        String search = request.getParameter("search");
        Product.Category category = parseCategory(request.getParameter("category"));
        request.setAttribute("products", productDAO.findAll(search, category));
        request.setAttribute("search", search == null ? "" : search);
        request.setAttribute("selectedCategory", category == null ? "" : category.name());
        request.getRequestDispatcher("/WEB-INF/views/products/list.jsp").forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Product product = readProduct(request);
        String error = validate(product);
        if (error != null) {
            request.setAttribute("product", product);
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/products/form.jsp").forward(request, response);
            return;
        }
        if (product.getProductId() == 0) {
            productDAO.create(product);
        } else if (!productDAO.update(product)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/products");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        long productId = parseId(request.getParameter("id"));
        boolean deleted = productDAO.deleteIfUnused(productId);
        String message = deleted ? "Product deleted" : "Product cannot be deleted because it has related records";
        response.sendRedirect(request.getContextPath() + "/products?message=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
    }

    private static Product readProduct(HttpServletRequest request) {
        Product product = new Product();
        String id = request.getParameter("productId");
        product.setProductId(id == null || id.isBlank() ? 0 : Long.parseLong(id));
        product.setProductCode(trim(request.getParameter("productCode")));
        product.setProductName(trim(request.getParameter("productName")));
        product.setUnit(trim(request.getParameter("unit")));
        product.setCategory(parseCategory(request.getParameter("category")));
        product.setUnitPrice(parseDecimal(request.getParameter("unitPrice")));
        product.setCurrentQuantity(parseDecimal(request.getParameter("currentQuantity")));
        product.setMinimumStock(parseDecimal(request.getParameter("minimumStock")));
        return product;
    }

    private static String validate(Product product) {
        if (product.getProductCode() == null || product.getProductCode().isBlank() || product.getProductCode().length() > 30) return "Product code is required and must be at most 30 characters.";
        if (product.getProductName() == null || product.getProductName().isBlank() || product.getProductName().length() > 120) return "Product name is required and must be at most 120 characters.";
        if (product.getCategory() == null) return "Choose a product category.";
        if (product.getUnit() == null || product.getUnit().isBlank() || product.getUnit().length() > 20) return "Unit is required and must be at most 20 characters.";
        if (product.getUnitPrice() == null || product.getUnitPrice().signum() < 0) return "Unit price must be zero or greater.";
        if (product.getCurrentQuantity() == null || product.getCurrentQuantity().signum() < 0) return "Current quantity must be zero or greater.";
        if (product.getMinimumStock() == null || product.getMinimumStock().signum() < 0) return "Minimum stock must be zero or greater.";
        return null;
    }

    private static long parseId(String value) {
        if (value == null || value.isBlank()) throw new NumberFormatException();
        return Long.parseLong(value);
    }

    private static BigDecimal parseDecimal(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private static Product.Category parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Product.Category.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}