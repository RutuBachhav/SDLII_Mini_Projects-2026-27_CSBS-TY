package com.web;

import com.dao.ClaimDao;
import com.dao.ItemDao;
import com.dao.MatchDao;
import com.entity.Claim;
import com.entity.Item;
import com.entity.User;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * One report in full.
 *
 * What a viewer gets depends on who they are: the identifying marks, the claim
 * list and the match suggestions are only attached for the reporter and for
 * administrators. Everyone else gets the public description.
 */
@WebServlet("/items/view")
public class ItemDetailServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();
    private final MatchDao matches = new MatchDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Web.num(request, "id", 0);
        if (id <= 0) {
            notFound(request, response, "That report");
            return;
        }
        try {
            Item item = items.findById(id);
            if (item == null) {
                notFound(request, response, "That report");
                return;
            }
            User user = Web.currentUser(request);
            boolean owner = user != null && user.getId() == item.getReporterId();
            boolean privileged = owner || (user != null && user.isAdmin());
            item.setCanSeePrivate(privileged);

            if (privileged) {
                request.setAttribute("matches", matches.forItem(item.getId(), item.getType()));
                List<Claim> itemClaims = claims.listByItem(item.getId());
                request.setAttribute("claims", itemClaims);
            }
            boolean canClaim = user != null && item.isFound() && item.isOpen() && !owner
                    && !claims.hasLiveClaim(item.getId(), user.getId());
            request.setAttribute("item", item);
            request.setAttribute("owner", owner);
            request.setAttribute("privileged", privileged);
            request.setAttribute("canClaim", canClaim);
            render(request, response, "item.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
