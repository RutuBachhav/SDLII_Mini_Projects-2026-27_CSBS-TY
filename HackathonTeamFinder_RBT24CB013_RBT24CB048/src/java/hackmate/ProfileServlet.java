package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lets a logged in user edit the profile.
 *
 * <p>The page can update the display name, the skill list and the password.
 * Changing the password asks for the current password first.</p>
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

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
            request.setAttribute("user", userDAO.findById(userId));
            request.getRequestDispatcher("/profile.jsp").forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Could not load the profile", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load your profile.");
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

        String fullName = trim(request.getParameter("fullName"));
        String skills = trim(request.getParameter("skills"));
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String currentPassword = request.getParameter("currentPassword");

        String error = null;
        boolean changed = false;

        if (fullName.isEmpty()) {
            error = "Your name cannot be empty.";
        } else if (fullName.length() > 100) {
            error = "Your name is too long (maximum 100 characters).";
        } else if (skills.length() > 255) {
            error = "Your skill list is too long (maximum 255 characters).";
        }

        boolean wantsNewPassword = newPassword != null && !newPassword.isEmpty();

        if (error == null && wantsNewPassword) {
            if (newPassword.length() < 8) {
                error = "The new password must be at least 8 characters long.";
            } else if (!newPassword.equals(confirmPassword)) {
                error = "The two new passwords do not match.";
            } else if (currentPassword == null || currentPassword.isEmpty()) {
                error = "Please enter your current password to change it.";
            }
        }

        if (error == null && wantsNewPassword) {
            try {
                User user = userDAO.findByEmail(
                        SessionUtil.getString(request, SessionUtil.USER_EMAIL, ""));

                // The current password has to be correct before a new one is stored.
                if (user == null || !PasswordUtil.verify(currentPassword, user.getPassword())) {
                    error = "Your current password is not correct.";
                }
            } catch (Exception e) {
                getServletContext().log("Could not verify the current password", e);
                error = "Something went wrong while checking your password.";
            }
        }

        if (error == null) {
            try {
                userDAO.updateProfile(userId, fullName, skills);
                changed = true;

                if (wantsNewPassword) {
                    userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
                }

                // Keep the session in step with the new name.
                request.getSession().setAttribute(SessionUtil.USER_NAME, fullName);

                JoinTeamServlet.flash(request,
                        wantsNewPassword ? "Profile and password updated." : "Profile updated.", true);

                response.sendRedirect(request.getContextPath() + "/profile");
                return;

            } catch (Exception e) {
                getServletContext().log("Could not update the profile", e);
                error = "Something went wrong while saving your profile.";
            }
        }

        try {
            User user = userDAO.findById(userId);
            request.setAttribute("user", user);
        } catch (Exception ignored) {
            // The page still shows the values that were typed in.
        }

        request.setAttribute("error", error);
        request.setAttribute("fullName", fullName);
        request.setAttribute("skills", skills);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
