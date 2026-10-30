package hackmate;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The landing page for a logged in user.
 *
 * <p>Shows the profile, the skill list and the teams the user belongs to.</p>
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.getUserId(request);

        // Not logged in, send the visitor to the login page.
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            User user = userDAO.findById(userId);

            // The account was removed while the session was still alive.
            if (user == null) {
                SessionUtil.logOut(request);
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            List<Team> teams = teamDAO.findTeamsForUser(userId);

            request.setAttribute("user", user);
            request.setAttribute("teams", teams);
            request.setAttribute("teamsLedCount", teamDAO.countTeamsLed(userId));
            request.setAttribute("myRequests", requestDAO.findForUser(userId));

            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Could not load the dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load your dashboard.");
        }
    }
}
