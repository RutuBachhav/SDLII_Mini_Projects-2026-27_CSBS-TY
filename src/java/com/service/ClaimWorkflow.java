package com.service;

import com.dao.AuditDao;
import com.dao.ClaimDao;
import com.dao.ItemDao;
import com.dao.MatchDao;
import com.dao.NotificationDao;
import com.dao.ReturnDao;
import com.util.Db;
import com.entity.Claim;
import com.entity.Item;
import com.entity.MatchSuggestion;
import com.entity.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The claim and return workflow: claim → review → return.
 *
 * Everything that touches more than one table runs inside one transaction, and
 * every status change is guarded by the status it is coming from, so an illegal
 * jump or a double click cannot leave half a workflow behind.
 */
public class ClaimWorkflow {

    /** Thrown for a rule the user broke; the message is safe to display. */
    public static class Refused extends Exception {
        public Refused(String message) {
            super(message);
        }
    }

    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();
    private final ReturnDao returns = new ReturnDao();
    private final NotificationDao notifications = new NotificationDao();
    private final MatchDao matches = new MatchDao();
    private final AuditDao audit = new AuditDao();

    // ------------------------------------------------------------------ submit

    public int submit(User claimant, int itemId, String evidence, String contact)
            throws SQLException, Refused {
        Item item = items.findById(itemId);
        if (item == null) {
            throw new Refused("That report no longer exists.");
        }
        if (!item.isFound()) {
            throw new Refused("Claims can only be submitted against found-item reports.");
        }
        if (!item.isOpen()) {
            throw new Refused("This report is already " + item.getStatus().toLowerCase()
                    + ", so it cannot be claimed.");
        }
        if (item.getReporterId() == claimant.getId()) {
            throw new Refused("You reported this item, so you cannot claim it.");
        }
        if (claims.hasLiveClaim(itemId, claimant.getId())) {
            throw new Refused("You already have a claim on this item. "
                    + "Check its status under My claims.");
        }

        int claimId = claims.insert(itemId, claimant.getId(), evidence, contact);
        notifications.add(item.getReporterId(), "CLAIM_NEW",
                claimant.getName() + " submitted a claim on \"" + item.getTitle() + "\".",
                "/claims/review", "claim-" + claimId);
        audit.log(claimant.getId(), "CLAIM_SUBMITTED", "claim", claimId,
                "Claim on item " + itemId);
        return claimId;
    }

    // ------------------------------------------------------------------ review

    /**
     * Approve or reject. Only the reporter of the claimed item or an
     * administrator may do this, and the check happens here on the server, not
     * by hiding a button.
     */
    public void decide(User reviewer, int claimId, boolean approve, String note)
            throws SQLException, Refused {
        Claim claim = claims.findById(claimId);
        if (claim == null) {
            throw new Refused("That claim no longer exists.");
        }
        if (!canReview(reviewer, claim)) {
            throw new Refused("You are not allowed to review this claim.");
        }
        if (!claim.isPending()) {
            throw new Refused("This claim was already " + claim.getStatus().toLowerCase() + ".");
        }

        Connection c = null;
        try {
            c = Db.openTransaction();
            String target = approve ? "APPROVED" : "REJECTED";
            if (!claims.transition(c, claimId, "PENDING", target, reviewer.getId(), note)) {
                Db.rollback(c);
                throw new Refused("This claim was just updated by somebody else.");
            }
            if (approve) {
                // Once one claim is approved the others on that item are closed,
                // otherwise two people could both be told they are the owner.
                claims.rejectOtherPending(c, claim.getItemId(), claimId, reviewer.getId());
            }
            notifications.add(c, claim.getClaimantId(),
                    approve ? "CLAIM_APPROVED" : "CLAIM_REJECTED",
                    approve
                            ? "Your claim on \"" + claim.getItemTitle()
                              + "\" was approved. Arrange the handover with "
                              + claim.getItemReporterName() + "."
                            : "Your claim on \"" + claim.getItemTitle() + "\" was rejected."
                              + (note == null || note.isBlank() ? "" : " Reason: " + note),
                    "/claims/mine", "claim-" + claimId + "-" + target);
            audit.log(c, reviewer.getId(), approve ? "CLAIM_APPROVED" : "CLAIM_REJECTED",
                    "claim", claimId, note);
            c.commit();
        } catch (SQLException e) {
            Db.rollback(c);
            throw e;
        } finally {
            close(c);
        }
    }

    // ------------------------------------------------------------------ return

    /**
     * Records the handover. This is the only place an item becomes RESOLVED
     * through a claim, and it happens after the item changed hands, never
     * because somebody submitted a claim.
     *
     * In one transaction: the claim becomes COMPLETED, the found report becomes
     * RESOLVED, the claimant's matching lost report is resolved too, a return
     * row is written, and both sides are notified.
     */
    public void recordReturn(User actor, int claimId, String note)
            throws SQLException, Refused {
        Claim claim = claims.findById(claimId);
        if (claim == null) {
            throw new Refused("That claim no longer exists.");
        }
        if (!canReview(actor, claim)) {
            throw new Refused("You are not allowed to record this return.");
        }
        if (!claim.isApproved()) {
            throw new Refused("Only an approved claim can be marked as returned.");
        }

        Connection c = null;
        try {
            c = Db.openTransaction();
            if (!claims.transition(c, claimId, "APPROVED", "COMPLETED",
                    actor.getId(), note)) {
                Db.rollback(c);
                throw new Refused("This claim was just updated by somebody else.");
            }
            if (!items.setStatus(c, claim.getItemId(), "RESOLVED", "OPEN")) {
                Db.rollback(c);
                throw new Refused("This item is no longer open, so the return was not recorded.");
            }
            returns.insert(c, claim.getItemId(), claimId, actor.getId(), note);

            notifications.add(c, claim.getClaimantId(), "RETURN_DONE",
                    "\"" + claim.getItemTitle() + "\" was marked as returned to you.",
                    "/items/view?id=" + claim.getItemId(), "ret-claim-" + claimId);
            notifications.add(c, claim.getItemReporterId(), "RETURN_DONE",
                    "You recorded the return of \"" + claim.getItemTitle() + "\". Case closed.",
                    "/items/view?id=" + claim.getItemId(),
                    "ret-claim-" + claimId + "-reporter");
            audit.log(c, actor.getId(), "RETURN_RECORDED", "item", claim.getItemId(),
                    "Claim " + claimId + " completed");
            c.commit();
        } catch (SQLException e) {
            Db.rollback(c);
            throw e;
        } finally {
            close(c);
        }

        // Housekeeping outside the transaction: the resolved item must disappear
        // from matching, and the claimant's own lost report can be closed if the
        // two were suggested to each other.
        resolveClaimantLostReport(claim);
        matches.deleteForItem(claim.getItemId());
    }

    private void resolveClaimantLostReport(Claim claim) throws SQLException {
        List<MatchSuggestion> pairs = matches.forItem(claim.getItemId(), "FOUND");
        for (MatchSuggestion m : pairs) {
            Item lost = m.getCounterpart();
            if (lost != null && lost.getReporterId() == claim.getClaimantId() && lost.isOpen()) {
                items.setStatus(lost.getId(), "RESOLVED", "OPEN");
                matches.deleteForItem(lost.getId());
                notifications.add(claim.getClaimantId(), "ITEM_RESOLVED",
                        "Your lost report \"" + lost.getTitle()
                        + "\" was closed because the item was returned to you.",
                        "/items/view?id=" + lost.getId(), "resolve-lost-" + lost.getId());
                return;
            }
        }
    }

    // ------------------------------------------------------------------ shared

    /** The reporter of the claimed item, or any administrator. */
    public boolean canReview(User user, Claim claim) {
        return user != null && (user.isAdmin() || user.getId() == claim.getItemReporterId());
    }

    /** Who may read the private evidence: claimant, item reporter, administrator. */
    public boolean canSeeEvidence(User user, Claim claim) {
        return user != null && (user.isAdmin()
                || user.getId() == claim.getClaimantId()
                || user.getId() == claim.getItemReporterId());
    }

    private static void close(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException ignored) {
                // nothing useful can be done here
            }
        }
    }
}
