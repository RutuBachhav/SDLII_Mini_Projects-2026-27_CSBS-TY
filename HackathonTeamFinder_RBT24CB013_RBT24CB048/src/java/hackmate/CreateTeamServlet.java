package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles the form that creates a new team.
 *
 * <p>Only a logged in user can create a team, because the creator becomes
 * the team leader.</p>
 */
@WebServlet("/create-team")
public class CreateTeamServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.getUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.getRequestDispatcher("/create-team.jsp").forward(request, response);
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

        String teamName = trim(request.getParameter("teamName"));
        String projectTitle = trim(request.getParameter("projectTitle"));
        String description = trim(request.getParameter("description"));
        String requiredSkills = trim(request.getParameter("requiredSkills"));

        String error = validate(teamName, projectTitle, description, requiredSkills);

        if (error == null) {
            try {
                int teamId = teamDAO.createTeam(teamName, projectTitle, description,
                        requiredSkills, userId);

                if (teamId > 0) {
                    response.sendRedirect(request.getContextPath() + "/team?id=" + teamId);
                    return;
                }
                error = "Could not create the team. Please try again.";
            } catch (Exception e) {
                getServletContext().log("Could not create the team", e);
                error = "Something went wrong while creating your team.";
            }
        }

        request.setAttribute("error", error);
        request.setAttribute("teamName", teamName);
        request.setAttribute("projectTitle", projectTitle);
        request.setAttribute("description", description);
        request.setAttribute("requiredSkills", requiredSkills);
        request.getRequestDispatcher("/create-team.jsp").forward(request, response);
    }

    private String validate(String teamName, String projectTitle, String description,
            String requiredSkills) {

        if (teamName.isEmpty()) {
            return "Please enter a team name.";
        }
        if (teamName.length() > 100) {
            return "The team name is too long (maximum 100 characters).";
        }
        if (projectTitle.isEmpty()) {
            return "Please enter a project title.";
        }
        if (projectTitle.length() > 150) {
            return "The project title is too long (maximum 150 characters).";
        }
        if (description.isEmpty()) {
            return "Please describe your project idea.";
        }
        if (description.length() > 2000) {
            return "The description is too long (maximum 2000 characters).";
        }
        if (requiredSkills.length() > 255) {
            return "The required skills list is too long (maximum 255 characters).";
        }
        return null;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
