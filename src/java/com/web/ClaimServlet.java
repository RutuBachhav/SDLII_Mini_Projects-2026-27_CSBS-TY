package com.web;

import com.dao.ClaimDao;
import com.dao.ItemDao;
import com.entity.Claim;
import com.entity.Item;
import com.entity.User;
import com.service.ClaimWorkflow;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Submitting a claim and looking at your own claims.
 *
 * The evidence a claimant types is private. It is stored as text in the first
 * version, which keeps the ownership question where it belongs, with a person
 * who verifies it, and avoids a second upload path that would need its own
 * protection.
 */
@WebServlet({"/claims/new", "/claims/mine"})
public class ClaimServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();
    private final ClaimWorkflow workflow = new ClaimWorkflow();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        try {
            if ("/claims/mine".equals(request.getServletPath())) {
                List<Claim> mine = claims.listByClaimant(user.getId());
                request.setAttribute("claims", mine);
                render(request, response, "claims-mine.jsp");
                return;
            }
            Item item = items.findById(Web.num(request, "item", 0));
            if (item == null) {
                notFound(request, response, "That report");
                return;
            }
            if (!item.isFound()) {
                redirect(request, response, "/items/view?id=" + item.getId(), "error",
                        "Claims are submitted against found-item reports.");
                return;
            }
            if (item.getReporterId() == user.getId()) {
                redirect(request, response, "/items/view?id=" + item.getId(), "error",
                        "You reported this item, so you cannot claim it.");
                return;
            }
            if (claims.hasLiveClaim(item.getId(), user.getId())) {
                redirect(request, response, "/claims/mine", "note",
                        "You already have a claim on that item.");
                return;
            }
            request.setAttribute("item", item);
            render(request, response, "claim-form.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        int itemId = Web.num(request, "itemId", 0);
        String evidence = Web.str(request, "evidence");
        String contact = Web.clip(Web.str(request, "contact"), 120);

        // One message per field that is actually wrong, so the list never tells
        // somebody to fix something they got right.
        List<String> errors = new ArrayList<>();
        if (evidence.length() < 25) {
            errors.add("Describe the item in at least 25 characters. Mention something only the "
                    + "owner would know.");
        } else if (evidence.length() > 2000) {
            errors.add("That description is too long. Keep it under 2000 characters.");
        }
        if (contact.length() < 5) {
            errors.add("Leave a phone number or email so the finder can reach you.");
        }

        if (!errors.isEmpty()) {
            try {
                Item item = items.findById(itemId);
                if (item == null) {
                    notFound(request, response, "That report");
                    return;
                }
                request.setAttribute("item", item);
                request.setAttribute("evidence", evidence);
                request.setAttribute("contact", contact);
                request.setAttribute("errors", errors);
                render(request, response, "claim-form.jsp");
            } catch (SQLException e) {
                fail(request, response, e);
            }
            return;
        }

        try {
            workflow.submit(user, itemId, evidence, contact);
            redirect(request, response, "/claims/mine", "success",
                    "Claim submitted. The finder will review it and you will be notified.");
        } catch (ClaimWorkflow.Refused refused) {
            redirect(request, response, "/items/view?id=" + itemId, "error",
                    refused.getMessage());
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
