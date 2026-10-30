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
import java.io.PrintWriter;
import java.util.List;

/**
 * Step 1 of update: look up a product by ID or by name/SKU and render a
 * pre-filled form that posts to /updateProduct. Values are HTML-escaped.
 */
@WebServlet("/getProductForUpdate")
public class GetProductForUpdateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Allow direct edit links like getProductForUpdate?id=3
        if (hasLookupParam(req)) {
            lookup(req, resp);
        } else {
            resp.sendRedirect("updateProduct.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        lookup(req, resp);
    }

    private void lookup(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        Long id = parseLong(trim(req.getParameter("id")));
        String name = trim(req.getParameter("name"));
        if (name == null) {
            name = trim(req.getParameter("q"));
        }

        if (id == null && name == null) {
            fail(resp, "Enter a Product ID or a Product Name / SKU.");
            return;
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            if (id != null) {
                Product p = em.find(Product.class, id);
                if (p == null) {
                    fail(resp, "Product with ID " + id + " not found.");
                    return;
                }
                renderEditForm(resp.getWriter(), p);
                return;
            }

            List<Product> matches = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category "
                    + "WHERE LOWER(p.name) LIKE :k OR LOWER(p.sku) LIKE :k "
                    + "ORDER BY p.name",
                    Product.class)
                    .setParameter("k", "%" + name.toLowerCase() + "%")
                    .getResultList();

            if (matches.isEmpty()) {
                fail(resp, "No product matching '" + name + "' found.");
            } else if (matches.size() == 1) {
                renderEditForm(resp.getWriter(), matches.get(0));
            } else {
                renderPicker(resp.getWriter(), name, matches);
            }
        } finally {
            em.close();
        }
    }

    private void renderEditForm(PrintWriter out, Product p) {
        String cat = p.getCategory() == null ? "" : p.getCategory().getName();
        out.println(Html.header("Update Product", "updateProduct.html")
                + "<div class='card form-card'><h3>Editing #" + p.getId()
                + " - " + Html.esc(p.getName()) + "</h3>"
                + "<form action='updateProduct' method='post'>"
                + "<input type='hidden' name='id' value='" + p.getId() + "'>"
                + "<label>SKU</label><input type='text' name='sku' value='"
                + Html.esc(p.getSku()) + "' required>"
                + "<label>Product Name</label><input type='text' name='name' value='"
                + Html.esc(p.getName()) + "' required>"
                + "<label>Price</label><input type='number' step='0.01' min='0.01' name='price' value='"
                + p.getPrice() + "' required>"
                + "<label>Quantity</label><input type='number' min='0' name='quantity' value='"
                + p.getQuantity() + "' required>"
                + "<label>Description</label><textarea name='description' rows='3'>"
                + Html.esc(p.getDescription() == null ? "" : p.getDescription())
                + "</textarea>"
                + "<p class='hint'>Category: <b>" + Html.esc(cat)
                + "</b> (change category from View Products / REST API)</p>"
                + "<button class='btn' type='submit'>Update Product</button>"
                + "</form></div>"
                + Html.footer());
    }

    private void renderPicker(PrintWriter out, String term, List<Product> matches) {
        out.println(Html.header("Update Product", "updateProduct.html")
                + Html.alert("info", matches.size()
                        + " products match '" + term + "'. Pick one to edit.")
                + "<div class='table-wrap'><table><thead><tr>"
                + "<th>ID</th><th>SKU</th><th>Name</th><th>Category</th>"
                + "<th>Price</th><th>Qty</th><th></th></tr></thead><tbody>");
        for (Product p : matches) {
            out.println("<tr><td>" + p.getId() + "</td>"
                    + "<td>" + Html.esc(p.getSku()) + "</td>"
                    + "<td>" + Html.esc(p.getName()) + "</td>"
                    + "<td>" + Html.esc(p.getCategory() == null
                            ? "-" : p.getCategory().getName()) + "</td>"
                    + "<td>$" + String.format("%.2f", p.getPrice()) + "</td>"
                    + "<td>" + p.getQuantity() + "</td>"
                    + "<td><form action='getProductForUpdate' method='post' style='margin:0'>"
                    + "<input type='hidden' name='id' value='" + p.getId() + "'>"
                    + "<button class='btn' type='submit'>Edit</button>"
                    + "</form></td></tr>");
        }
        out.println("</tbody></table></div>"
                + "<p><a class='btn secondary' href='updateProduct.html'>Back</a></p>"
                + Html.footer());
    }

    private void fail(HttpServletResponse resp, String msg) throws IOException {
        resp.getWriter().println(Html.header("Update Product", "updateProduct.html")
                + Html.alert("error", msg)
                + "<p><a class='btn secondary' href='updateProduct.html'>Back</a></p>"
                + Html.footer());
    }

    private static boolean hasLookupParam(HttpServletRequest req) {
        return trim(req.getParameter("id")) != null
                || trim(req.getParameter("name")) != null
                || trim(req.getParameter("q")) != null;
    }

    private static Long parseLong(String s) {
        try {
            return s == null ? null : Long.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }
}
