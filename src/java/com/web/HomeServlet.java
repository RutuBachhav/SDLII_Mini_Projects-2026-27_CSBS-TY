package com.web;

import com.dao.CategoryDao;
import com.dao.ItemDao;
import com.dao.ReturnDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** The landing page. Every number and card on it comes from MySQL. */
@WebServlet("/home")
public class HomeServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final CategoryDao categories = new CategoryDao();
    private final ReturnDao returns = new ReturnDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("recent", items.recent(6));
            request.setAttribute("categories", categories.listActive());
            request.setAttribute("openLost", items.countByTypeAndStatus("LOST", "OPEN"));
            request.setAttribute("openFound", items.countByTypeAndStatus("FOUND", "OPEN"));
            request.setAttribute("returned", returns.countAll());
            render(request, response, "home.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
