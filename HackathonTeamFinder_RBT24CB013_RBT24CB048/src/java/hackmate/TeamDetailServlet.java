package hackmate;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shows the details of one team.
 *
 * <p>The page is public so anybody can look at a team, but the join button
 * needs a logged in user.</p>
 */
@WebServlet("/team")
public class TeamDetailServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int teamId = parseId(request.getParameter("id"));

        if (teamId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown team.");
            return;
        }

        try {
            Team team = teamDAO.findById(teamId);

            if (team == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "This team does not exist.");
                return;
            }

            int userId = SessionUtil.getUserId(request);
            boolean loggedIn = userId > 0;
            boolean isMember = loggedIn && teamDAO.isMember(teamId, userId);
            boolean isLeader = team.getTeamLeaderId() == userId;

            // Only a leader sees the list of join requests for the team.
            String requestStatus = null;
            if (loggedIn && !isMember) {
                requestStatus = requestDAO.findExistingStatus(teamId, userId);
            }

            request.setAttribute("team", team);
            request.setAttribute("members", teamDAO.findMembers(teamId));
            request.setAttribute("loggedIn", loggedIn);
            request.setAttribute("isMember", isMember);
            request.setAttribute("isLeader", isLeader);
            request.setAttribute("requestStatus", requestStatus);

            request.getRequestDispatcher("/team-detail.jsp").forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Could not load the team", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load this team.");
        }
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
}
