package hackmate;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The join request inbox of a team leader.
 *
 * <p>GET lists every request that was sent to the teams the logged in user
 * leads. POST accepts or rejects one of those requests.</p>
 */
@WebServlet("/team-requests")
public class TeamRequestsServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.getUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            List<TeamRequest> requests = requestDAO.findForLeader(userId);

            request.setAttribute("requests", requests);
            request.setAttribute("pendingCount", countPending(requests));

            request.getRequestDispatcher("/team-requests.jsp").forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Could not load the join requests", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load the join requests.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.getUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int requestId = parseId(request.getParameter("requestId"));
        String action = trim(request.getParameter("action"));

        try {
            TeamRequest joinRequest = requestDAO.findById(requestId);

            if (joinRequest == null) {
                JoinTeamServlet.flash(request, "That request no longer exists.", false);
            } else if (!isLeaderOf(userId, joinRequest.getTeamId())) {
                // Only the leader of that team may decide on the request.
                JoinTeamServlet.flash(request, "You can only manage requests for your own teams.", false);
            } else if (!TeamRequest.STATUS_PENDING.equals(joinRequest.getStatus())) {
                JoinTeamServlet.flash(request, "That request was already handled.", false);
            } else if ("accept".equals(action)) {
                requestDAO.updateStatus(requestId, TeamRequest.STATUS_ACCEPTED);
                requestDAO.addMember(joinRequest.getTeamId(), joinRequest.getUserId(), "Member");
                JoinTeamServlet.flash(request,
                        joinRequest.getRequesterName() + " is now a member of the team.", true);
            } else if ("reject".equals(action)) {
                requestDAO.updateStatus(requestId, TeamRequest.STATUS_REJECTED);
                JoinTeamServlet.flash(request,
                        "The request from " + joinRequest.getRequesterName() + " was rejected.", false);
            } else {
                JoinTeamServlet.flash(request, "Unknown action.", false);
            }

        } catch (Exception e) {
            getServletContext().log("Could not handle the join request", e);
            JoinTeamServlet.flash(request, "Something went wrong while handling the request.", false);
        }

        response.sendRedirect(request.getContextPath() + "/team-requests");
    }

    /**
     * @return true when the given user leads the given team
     */
    private boolean isLeaderOf(int userId, int teamId) throws Exception {
        Team team = new TeamDAO().findById(teamId);
        return team != null && team.getTeamLeaderId() == userId;
    }

    private static int countPending(List<TeamRequest> requests) {
        int count = 0;
        for (TeamRequest request : requests) {
            if (TeamRequest.STATUS_PENDING.equals(request.getStatus())) {
                count++;
            }
        }
        return count;
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
