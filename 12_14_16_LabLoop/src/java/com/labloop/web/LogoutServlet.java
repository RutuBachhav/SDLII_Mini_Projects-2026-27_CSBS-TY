package com.labloop.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        me.logout();
        req.getSession().invalidate();   // also destroys this session's Stateful kit bean (@PreDestroy runs)
        redirect(req, resp, "/login");
    }
}
