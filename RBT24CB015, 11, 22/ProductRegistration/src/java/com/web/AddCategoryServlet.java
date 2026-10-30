package com.web;

import com.entity.Category;
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
 * Creates a category. Uses Bean Validation manually + duplicate-name check
 * (Post-Redirect-Get to avoid double submit).
 */
@WebServlet("/addCategory")
public class AddCategoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect("addCategory.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String name = trim(req.getParameter("name"));
        String description = trim(req.getParameter("description"));

        if (name == null || name.length() < 2 || name.length() > 60) {
            render(resp, "Add Category", Html.alert("error",
                    "Category name must be 2-60 characters."));
            return;
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Long dup = em.createQuery(
                    "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = :n",
                    Long.class)
                    .setParameter("n", name.toLowerCase())
                    .getSingleResult();
            if (dup > 0) {
                render(resp, "Add Category", Html.alert("error",
                        "Category '" + name + "' already exists."));
                return;
            }

            Category c = new Category();
            c.setName(name);
            c.setDescription(description);

            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();

            resp.sendRedirect("addCategory.html?success=" + c.getId());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            render(resp, "Add Category",
                    Html.alert("error", "Unable to save category: " + e.getMessage()));
        } finally {
            em.close();
        }
    }

    private void render(HttpServletResponse resp, String title, String body)
            throws IOException {
        resp.getWriter().println(Html.header(title, "addCategory.html")
                + body
                + "<p><a class='btn secondary' href='addCategory.html'>Back</a></p>"
                + Html.footer());
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }

    /** Used by the dashboard / REST to list categories. */
    public static List<Category> all() {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.name",
                    Category.class).getResultList();
        } finally {
            em.close();
        }
    }
}
