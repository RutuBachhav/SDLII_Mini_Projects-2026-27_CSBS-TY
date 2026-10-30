package hackmate;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/TestDBConnection")
public class TestDBConnection extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            try (Connection con = DBConnection.getConnection()) {
                out.println("<h2>Database connected successfully!</h2>");
            } catch (Exception e) {
                out.println("<h2>Database connection failed.</h2>");
                out.println("<p>" + e.getMessage() + "</p>");
            }
        }
    }
}