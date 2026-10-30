package hackmate;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles the login form.
 *
 * <p>GET forwards to login.html, POST checks the credentials and starts a
 * session for the user.</p>
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = trim(request.getParameter("email")).toLowerCase();
        String password = request.getParameter("password");

        String error = null;
        User user = null;

        if (email.isEmpty() || password == null || password.isEmpty()) {
            error = "Please enter both your email address and your password.";
        } else {
            try {
                user = userDAO.findByEmail(email);
                if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
                    // Same message for both cases so the form does not reveal
                    // which email addresses are registered.
                    error = "Incorrect email address or password.";
                    user = null;
                }
            } catch (Exception e) {
                getServletContext().log("Login failed", e);
                error = "Something went wrong. Please try again.";
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        SessionUtil.logIn(request, user.getUserId(), user.getFullName(), user.getEmail());

        // Send the user back to the page they wanted before they had to log in.
        String target = SessionUtil.getString(request, "redirectAfterLogin", "");
        request.getSession().removeAttribute("redirectAfterLogin");

        if (target.isEmpty() || !target.startsWith(request.getContextPath() + "/")
                || target.startsWith(request.getContextPath() + "/logout")) {
            target = request.getContextPath() + "/dashboard";
        }

        response.sendRedirect(target);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
