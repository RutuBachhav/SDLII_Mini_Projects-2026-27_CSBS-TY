package com.web;

import com.entity.Category;
import com.entity.Product;
import com.util.Html;
import com.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Creates a product. Validates every field server-side (the Employee sample
 * defined constraints on the entity but never validated them and parsed
 * numbers without checks).
 */
@WebServlet("/saveProduct")
public class AddProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String sku = trim(req.getParameter("sku"));
        String name = trim(req.getParameter("name"));
        String priceRaw = trim(req.getParameter("price"));
        String qtyRaw = trim(req.getParameter("quantity"));
        String description = trim(req.getParameter("description"));
        String catRaw = trim(req.getParameter("categoryId"));

        String error = validate(sku, name, priceRaw, qtyRaw);
        Double price = parseDouble(priceRaw);
        Integer qty = parseInt(qtyRaw);
        Integer catId = parseInt(catRaw);
        if (error == null && catId == null) {
            error = "Please select a category.";
        }
        if (error != null) {
            fail(resp, error);
            return;
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Long dup = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE LOWER(p.sku) = :s",
                    Long.class)
                    .setParameter("s", sku.toLowerCase())
                    .getSingleResult();
            if (dup > 0) {
                fail(resp, "SKU '" + sku + "' is already used.");
                return;
            }

            Category category = em.find(Category.class, catId);
            if (category == null) {
                fail(resp, "Selected category does not exist.");
                return;
            }

            Product p = new Product();
            p.setSku(sku);
            p.setName(name);
            p.setPrice(price);
            p.setQuantity(qty);
            p.setDescription(description);
            p.setCategory(category);

            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();

            resp.getWriter().println(Html.header("Product Saved", "addProduct.html")
                    + Html.alert("success", "Product saved successfully (ID " + p.getId() + ").")
                    + "<div class='card'><h3>" + Html.esc(p.getName()) + "</h3>"
                    + "<p><b>SKU:</b> " + Html.esc(p.getSku()) + "<br>"
                    + "<b>Category:</b> " + Html.esc(category.getName()) + "<br>"
                    + "<b>Price:</b> $" + String.format("%.2f", p.getPrice()) + "<br>"
                    + "<b>Quantity:</b> " + p.getQuantity() + "</p>"
                    + "<a class='btn' href='addProduct.html'>Add Another</a> "
                    + "<a class='btn secondary' href='viewProducts.html'>View Products</a></div>"
                    + Html.footer());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            fail(resp, "Unable to save product: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private static String validate(String sku, String name,
                                   String priceRaw, String qtyRaw) {
        if (sku == null || !sku.matches("^[A-Za-z0-9-]{3,20}$")) {
            return "SKU must be 3-20 chars (letters, digits, dash).";
        }
        if (name == null || name.length() < 3 || name.length() > 80) {
            return "Product name must be 3-80 characters.";
        }
        Double price = parseDouble(priceRaw);
        if (price == null || price < 0.01) {
            return "Price must be a number greater than 0.";
        }
        Integer qty = parseInt(qtyRaw);
        if (qty == null || qty < 0) {
            return "Quantity must be 0 or more.";
        }
        return null;
    }

    private void fail(HttpServletResponse resp, String msg) throws IOException {
        resp.getWriter().println(Html.header("Add Product", "addProduct.html")
                + Html.alert("error", msg)
                + "<p><a class='btn secondary' href='addProduct.html'>Back</a></p>"
                + Html.footer());
    }

    private static Double parseDouble(String s) {
        try {
            return s == null ? null : Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String s) {
        try {
            return s == null ? null : Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }
}
