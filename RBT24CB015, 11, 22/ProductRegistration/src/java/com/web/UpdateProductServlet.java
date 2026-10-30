package com.web;

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
 * Step 2 of update: applies the edit. This servlet did not exist at all in
 * the Employee sample (its form posted to a missing 'updateEmployee' URL).
 */
@WebServlet("/updateProduct")
public class UpdateProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        Long id;
        try {
            id = Long.valueOf(req.getParameter("id"));
        } catch (Exception e) {
            fail(resp, "Invalid product ID.");
            return;
        }

        String sku = trim(req.getParameter("sku"));
        String name = trim(req.getParameter("name"));
        Double price;
        Integer qty;
        try {
            price = Double.valueOf(req.getParameter("price"));
            qty = Integer.valueOf(req.getParameter("quantity"));
        } catch (Exception e) {
            fail(resp, "Price and quantity must be valid numbers.");
            return;
        }
        String description = trim(req.getParameter("description"));

        if (sku == null || !sku.matches("^[A-Za-z0-9-]{3,20}$")) {
            fail(resp, "SKU must be 3-20 chars (letters, digits, dash).");
            return;
        }
        if (name == null || name.length() < 3 || name.length() > 80) {
            fail(resp, "Product name must be 3-80 characters.");
            return;
        }
        if (price < 0.01 || qty < 0) {
            fail(resp, "Price must be > 0 and quantity >= 0.");
            return;
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Product p = em.find(Product.class, id);
            if (p == null) {
                fail(resp, "Product with ID " + id + " not found.");
                return;
            }
            Long dup = em.createQuery(
                    "SELECT COUNT(x) FROM Product x WHERE LOWER(x.sku) = :s AND x.id <> :id",
                    Long.class)
                    .setParameter("s", sku.toLowerCase())
                    .setParameter("id", id)
                    .getSingleResult();
            if (dup > 0) {
                fail(resp, "SKU '" + sku + "' is already used by another product.");
                return;
            }

            em.getTransaction().begin();
            p.setSku(sku);
            p.setName(name);
            p.setPrice(price);
            p.setQuantity(qty);
            p.setDescription(description);
            em.getTransaction().commit();

            resp.getWriter().println(Html.header("Product Updated", "updateProduct.html")
                    + Html.alert("success",
                            "Product #" + p.getId() + " updated successfully.")
                    + "<p><a class='btn' href='viewProducts.html'>View Products</a> "
                    + "<a class='btn secondary' href='index.html'>Dashboard</a></p>"
                    + Html.footer());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            fail(resp, "Unable to update product: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private void fail(HttpServletResponse resp, String msg) throws IOException {
        resp.getWriter().println(Html.header("Update Product", "updateProduct.html")
                + Html.alert("error", msg)
                + "<p><a class='btn secondary' href='updateProduct.html'>Back</a></p>"
                + Html.footer());
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }
}
