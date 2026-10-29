package com.web;

import com.dao.ClaimDao;
import com.entity.Claim;
import com.entity.User;
import com.service.ClaimWorkflow;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * The review inbox, the approve/reject decision and the return record.
 *
 * Authorisation is decided by ClaimWorkflow on every POST, so a copied URL or a
 * crafted form cannot review somebody else's claim.
 */
@WebServlet({"/claims/review", "/claims/decide", "/claims/return"})
public class ClaimReviewServlet extends BaseServlet {

    private final ClaimDao claims = new ClaimDao();
    private final ClaimWorkflow workflow = new ClaimWorkflow();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        try {
            List<Claim> inbox = claims.listForReviewer(user.getId());
            request.setAttribute("claims", inbox);
            render(request, response, "claims-review.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        int claimId = Web.num(request, "claimId", 0);
        String note = Web.clip(Web.str(request, "note"), 255);
        String back = Web.str(request, "back");
        String target = back.startsWith("/") && !back.startsWith("//") ? back : "/claims/review";

        try {
            if ("/claims/return".equals(request.getServletPath())) {
                workflow.recordReturn(user, claimId, note);
                redirect(request, response, target, "success",
                        "Return recorded. The item is now marked as resolved.");
                return;
            }
            boolean approve = "approve".equals(Web.str(request, "decision"));
            workflow.decide(user, claimId, approve, note);
            redirect(request, response, target, "success",
                    approve
                            ? "Claim approved. Arrange the handover, then record the return."
                            : "Claim rejected.");
        } catch (ClaimWorkflow.Refused refused) {
            redirect(request, response, target, "error", refused.getMessage());
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
