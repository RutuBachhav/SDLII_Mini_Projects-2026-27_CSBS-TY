package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles the "Request to join" button on the team page.
 *
 * <p>A request is always sent to the team leader, who decides whether the
 * student is accepted.</p>
 */
@WebServlet("/join-team")
public class JoinTeamServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.getUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int teamId = parseId(request.getParameter("teamId"));
        String message = trim(request.getParameter("message"));

        if (teamId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown team.");
            return;
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }

        try {
            Team team = teamDAO.findById(teamId);

            if (team == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "This team does not exist.");
                return;
            }

            if (teamDAO.isMember(teamId, userId)) {
                flash(request, "You are already a member of this team.", false);
                response.sendRedirect(request.getContextPath() + "/team?id=" + teamId);
                return;
            }

            String existing = requestDAO.findExistingStatus(teamId, userId);
            if (TeamRequest.STATUS_PENDING.equals(existing)) {
                flash(request, "You already sent a request to this team.", false);
                response.sendRedirect(request.getContextPath() + "/team?id=" + teamId);
                return;
            }
            if (TeamRequest.STATUS_ACCEPTED.equals(existing)) {
                flash(request, "You are already a member of this team.", false);
                response.sendRedirect(request.getContextPath() + "/team?id=" + teamId);
                return;
            }

            requestDAO.createRequest(teamId, userId, message);
            flash(request, "Your request was sent to the team leader.", true);

        } catch (Exception e) {
            getServletContext().log("Could not send the join request", e);
            flash(request, "Something went wrong while sending your request.", false);
        }

        response.sendRedirect(request.getContextPath() + "/team?id=" + teamId);
    }

    /**
     * Stores a short message that is shown once on the next page.
     */
    static void flash(HttpServletRequest request, String message, boolean success) {
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", message);
    }

    private static int parseId(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
