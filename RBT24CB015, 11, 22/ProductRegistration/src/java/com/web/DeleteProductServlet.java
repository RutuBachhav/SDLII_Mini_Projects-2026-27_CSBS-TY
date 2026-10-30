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
import java.util.List;

/**
 * Deletes a product by ID or by product name, with proper not-found +
 * transaction handling.
 */
@WebServlet("/deleteProduct")
public class DeleteProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String idParam = trimToNull(req.getParameter("id"));
        String nameParam = trimToNull(req.getParameter("name"));

        if (idParam == null && nameParam == null) {
            fail(resp, "Provide a product ID or a product name.");
            return;
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            if (idParam != null) {
                Long id;
                try {
                    id = Long.valueOf(idParam);
                } catch (NumberFormatException e) {
                    fail(resp, "Invalid product ID.");
                    return;
                }
                Product p = em.find(Product.class, id);
                if (p == null) {
                    fail(resp, "Product with ID " + id + " not found.");
                    return;
                }
                deleteAndReport(resp, em, p);
            } else {
                List<Product> matches = em.createQuery(
                        "SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:n) ORDER BY p.id",
                        Product.class)
                        .setParameter("n", nameParam)
                        .getResultList();
                if (matches.isEmpty()) {
                    fail(resp, "No product named \"" + nameParam + "\" found.");
                    return;
                }
                if (matches.size() > 1) {
                    showMatches(resp, matches);
                    return;
                }
                deleteAndReport(resp, em, matches.get(0));
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            fail(resp, "Unable to delete product: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private void deleteAndReport(HttpServletResponse resp, EntityManager em, Product p)
            throws IOException {
        String label = p.getName() + " (" + p.getSku() + ")";
        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();

        resp.getWriter().println(Html.header("Product Deleted", "deleteProduct.html")
                + Html.alert("success", "Deleted: " + label)
                + "<p><a class='btn secondary' href='deleteProduct.html'>Delete Another</a> "
                + "<a class='btn secondary' href='index.html'>Dashboard</a></p>"
                + Html.footer());
    }

    /** Several products share the name: let the user pick which one to delete. */
    private void showMatches(HttpServletResponse resp, List<Product> matches) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(Html.header("Delete Product", "deleteProduct.html"))
                .append(Html.alert("info", "More than one product has that name. Choose the one to delete:"))
                .append("<div class='table-wrap'><table><thead><tr>"
                        + "<th>ID</th><th>SKU</th><th>Name</th><th>Category</th><th></th>"
                        + "</tr></thead><tbody>");
        for (Product p : matches) {
            sb.append("<tr><td>").append(p.getId()).append("</td>")
                    .append("<td>").append(Html.esc(p.getSku())).append("</td>")
                    .append("<td>").append(Html.esc(p.getName())).append("</td>")
                    .append("<td>")
                    .append(p.getCategory() == null ? "-" : Html.esc(p.getCategory().getName()))
                    .append("</td><td>")
                    .append("<form action='deleteProduct' method='post' ")
                    .append("onsubmit=\"return confirm('Delete this product permanently?')\">")
                    .append("<input type='hidden' name='id' value='").append(p.getId()).append("'>")
                    .append("<button class='btn danger' type='submit'>Delete</button>")
                    .append("</form></td></tr>");
        }
        sb.append("</tbody></table></div>")
                .append("<p><a class='btn secondary' href='deleteProduct.html'>Back</a></p>")
                .append(Html.footer());
        resp.getWriter().println(sb);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void fail(HttpServletResponse resp, String msg) throws IOException {
        resp.getWriter().println(Html.header("Delete Product", "deleteProduct.html")
                + Html.alert("error", msg)
                + "<p><a class='btn secondary' href='deleteProduct.html'>Back</a></p>"
                + Html.footer());
    }
}
