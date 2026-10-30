package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles the registration form.
 *
 * <p>GET forwards to register.html, POST validates the input, creates the
 * account and redirects to the login page.</p>
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final String EMAIL_PATTERN = "^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email")).toLowerCase();
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String skills = trim(request.getParameter("skills"));

        String error = validate(fullName, email, password, confirmPassword, skills);

        if (error == null) {
            try {
                if (userDAO.emailExists(email)) {
                    error = "An account with this email address already exists.";
                } else {
                    String hashed = PasswordUtil.hash(password);
                    int userId = userDAO.createUser(fullName, email, hashed, skills);

                    if (userId > 0) {
                        request.getSession().setAttribute("flashSuccess",
                                "Account created. You can log in now.");
                        response.sendRedirect(request.getContextPath() + "/login");
                        return;
                    }
                    error = "Could not create the account. Please try again.";
                }
            } catch (Exception e) {
                // Never expose database internals to the browser.
                getServletContext().log("Registration failed", e);
                error = "Something went wrong while creating your account.";
            }
        }

        request.setAttribute("error", error);
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("skills", skills);
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    /**
     * @return null when everything is valid, otherwise a message for the user
     */
    private String validate(String fullName, String email, String password,
            String confirmPassword, String skills) {

        if (fullName.isEmpty()) {
            return "Please enter your full name.";
        }
        if (fullName.length() > 100) {
            return "Your name is too long (maximum 100 characters).";
        }
        if (email.isEmpty()) {
            return "Please enter your email address.";
        }
        if (!email.matches(EMAIL_PATTERN)) {
            return "Please enter a valid email address.";
        }
        if (email.length() > 100) {
            return "Your email address is too long (maximum 100 characters).";
        }
        if (password == null || password.isEmpty()) {
            return "Please choose a password.";
        }
        if (password.length() < 8) {
            return "Your password must be at least 8 characters long.";
        }
        if (password.length() > 72) {
            return "Your password is too long (maximum 72 characters).";
        }
        if (!password.equals(confirmPassword)) {
            return "The two passwords do not match.";
        }
        if (skills.length() > 255) {
            return "Your skill list is too long (maximum 255 characters).";
        }
        return null;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
