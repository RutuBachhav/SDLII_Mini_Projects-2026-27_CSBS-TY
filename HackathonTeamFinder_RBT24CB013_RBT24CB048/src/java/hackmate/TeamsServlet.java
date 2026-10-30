package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shows the list of teams, with an optional skill filter and text search.
 *
 * <p>The page is public, anybody can browse the teams.</p>
 */
@WebServlet("/teams")
public class TeamsServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String skill = trim(request.getParameter("skill"));
        String search = trim(request.getParameter("q"));

        try {
            request.setAttribute("teams", teamDAO.listTeams(skill, search));
            request.setAttribute("skill", skill);
            request.setAttribute("q", search);
            request.setAttribute("loggedIn", SessionUtil.isLoggedIn(request));

            request.getRequestDispatcher("/teams.jsp").forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Could not load the team list", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load the teams.");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
