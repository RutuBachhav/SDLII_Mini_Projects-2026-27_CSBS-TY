package com.labloop.web;

import com.labloop.entity.LabUser;
import com.labloop.service.UserService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (me.isLoggedIn()) {
            redirect(req, resp, "/dashboard");
            return;
        }
        render(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = str(req, "email");
        LabUser user = userService.authenticate(email, req.getParameter("password"));
        if (user == null) {
            req.setAttribute("error", "Wrong email or password.");
            req.setAttribute("email", email);
            render(req, resp, "login");
            return;
        }
        req.changeSessionId();          // protects against session-fixation attacks
        me.login(user);
        String next = req.getParameter("next");
        // only allow redirects inside LabLoop
        if (next != null && next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/login")) {
            redirect(req, resp, next);
        } else {
            redirect(req, resp, "/dashboard");
        }
    }
}
