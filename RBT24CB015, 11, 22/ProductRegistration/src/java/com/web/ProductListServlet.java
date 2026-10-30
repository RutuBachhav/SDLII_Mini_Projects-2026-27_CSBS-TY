package com.web;

import com.entity.Category;
import com.entity.Product;
import com.util.Html;
import com.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Server-rendered product list with search (q) + category filter.
 * This replaces the empty ViewEmployeeServlet from the Employee sample.
 */
@WebServlet("/viewProducts")
public class ProductListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");

        String q = trim(req.getParameter("q"));
        Integer categoryId = parseInt(trim(req.getParameter("categoryId")));

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            List<Category> categories = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.name",
                    Category.class).getResultList();

            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE 1=1");
            if (q != null) {
                jpql.append(" AND (LOWER(p.name) LIKE :q OR LOWER(p.sku) LIKE :q)");
            }
            if (categoryId != null) {
                jpql.append(" AND p.category.id = :cid");
            }
            jpql.append(" ORDER BY p.id DESC");

            TypedQuery<Product> query = em.createQuery(
                    jpql.toString(), Product.class);
            if (q != null) {
                query.setParameter("q", "%" + q.toLowerCase() + "%");
            }
            if (categoryId != null) {
                query.setParameter("cid", categoryId);
            }
            List<Product> products = query.getResultList();

            double totalValue = 0;
            for (Product p : products) {
                totalValue += p.getStockValue();
            }

            PrintWriter out = resp.getWriter();
            out.println(Html.header("View Products", "viewProducts.html"));
            out.println("<div class='toolbar'><form method='get' action='viewProducts' class='filters'>"
                    + "<input type='text' name='q' placeholder='Search name or SKU' value='"
                    + Html.esc(q == null ? "" : q) + "'>"
                    + "<select name='categoryId'><option value=''>All Categories</option>");
            for (Category c : categories) {
                String sel = (categoryId != null && categoryId.equals(c.getId()))
                        ? " selected" : "";
                out.println("<option value='" + c.getId() + "'" + sel + ">"
                        + Html.esc(c.getName()) + "</option>");
            }
            out.println("</select><button class='btn' type='submit'>Search</button></form>"
                    + "<div class='stat'>Total: <b>" + products.size() + "</b> products | "
                    + "Stock value: <b>$" + String.format("%.2f", totalValue) + "</b></div></div>");

            if (products.isEmpty()) {
                out.println(Html.alert("info", "No products found."));
            } else {
                out.println("<div class='table-wrap'><table><thead><tr>"
                        + "<th>ID</th><th>SKU</th><th>Name</th><th>Category</th>"
                        + "<th>Price</th><th>Qty</th><th>Value</th><th>Status</th><th></th></tr></thead><tbody>");
                for (Product p : products) {
                    String badge = p.getQuantity() < 10
                            ? "<span class='badge low'>Low stock</span>"
                            : "<span class='badge ok'>In stock</span>";
                    out.println("<tr><td>" + p.getId() + "</td>"
                            + "<td>" + Html.esc(p.getSku()) + "</td>"
                            + "<td>" + Html.esc(p.getName()) + "</td>"
                            + "<td>" + Html.esc(p.getCategory() == null
                                    ? "-" : p.getCategory().getName()) + "</td>"
                            + "<td>$" + String.format("%.2f", p.getPrice()) + "</td>"
                            + "<td>" + p.getQuantity() + "</td>"
                            + "<td>$" + String.format("%.2f", p.getStockValue()) + "</td>"
                            + "<td>" + badge + "</td>"
                            + "<td><a class='btn secondary' href='getProductForUpdate?id="
                            + p.getId() + "'>Edit</a></td></tr>");
                }
                out.println("</tbody></table></div>");
            }
            out.println(Html.footer());
        } finally {
            em.close();
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }

    private static Integer parseInt(String s) {
        try {
            return s == null ? null : Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
