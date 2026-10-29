package com.web;

import com.dao.CategoryDao;
import com.dao.ItemDao;
import com.dao.ItemQuery;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Search, filter and page through reports. All of it is SQL, none of it is cached. */
@WebServlet("/browse")
public class BrowseServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final CategoryDao categories = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ItemQuery q = new ItemQuery();
        q.setKeyword(Web.str(request, "q"));
        q.setType(Web.str(request, "type"));
        q.setCategoryId(Web.num(request, "category", 0));
        q.setLocation(Web.str(request, "location"));
        String status = Web.str(request, "status");
        q.setStatus(status.isEmpty() ? "OPEN" : status);
        q.setDateFrom(Web.str(request, "from"));
        q.setDateTo(Web.str(request, "to"));
        q.setSort(Web.str(request, "sort"));
        q.setPage(Web.num(request, "page", 1));

        try {
            int total = items.count(q);
            int pages = Math.max(1, (int) Math.ceil(total / (double) q.getPageSize()));
            if (q.getPage() > pages) {
                q.setPage(pages);
            }
            request.setAttribute("q", q);
            request.setAttribute("results", items.search(q));
            request.setAttribute("total", total);
            request.setAttribute("pages", pages);
            request.setAttribute("categories", categories.listActive());
            request.setAttribute("locations", items.locations());
            render(request, response, "browse.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
