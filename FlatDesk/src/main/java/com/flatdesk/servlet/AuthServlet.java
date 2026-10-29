package com.flatdesk.servlet;

import com.flatdesk.config.DB;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private void json(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(object));
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        try {
            JsonLogin login =
                    gson.fromJson(request.getReader(), JsonLogin.class);

            String query =
                    "SELECT id, name, email, role, password " +
                    "FROM users WHERE email = ?";

            try (Connection connection = DB.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(query)) {

                statement.setString(1, login.email);

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        response.setStatus(401);
                        json(response,
                                new Msg("Invalid email or password"));
                        return;
                    }

                    String storedPassword = result.getString("password");

                    if (!sha256(login.password).equals(storedPassword)) {
                        response.setStatus(401);
                        json(response,
                                new Msg("Invalid email or password"));
                        return;
                    }

                    HttpSession session = request.getSession();

                    session.setAttribute(
                            "userId",
                            result.getInt("id")
                    );

                    session.setAttribute(
                            "role",
                            result.getString("role")
                    );

                    session.setAttribute(
                            "name",
                            result.getString("name")
                    );

                    json(
                            response,
                            new LoginResponse(
                                    result.getInt("id"),
                                    result.getString("name"),
                                    result.getString("email"),
                                    result.getString("role")
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            json(
                    response,
                    new Msg("Server error: " + e.getMessage())
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(401);

            json(
                    response,
                    new Msg("Not logged in")
            );

            return;
        }

        json(
                response,
                new LoginResponse(
                        (Integer) session.getAttribute("userId"),
                        (String) session.getAttribute("name"),
                        "",
                        String.valueOf(session.getAttribute("role"))
                )
        );
    }

    private String sha256(String value) {

        try {

            java.security.MessageDigest md =
                    java.security.MessageDigest.getInstance("SHA-256");

            byte[] bytes =
                    md.digest(
                            value.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result = new StringBuilder();

            for (byte b : bytes) {

                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    static class JsonLogin {

        String email;
        String password;
    }

    record Msg(String message) {
    }

    record LoginResponse(
            int id,
            String name,
            String email,
            String role
    ) {
    }
}