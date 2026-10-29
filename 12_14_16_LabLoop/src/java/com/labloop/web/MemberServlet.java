package com.labloop.web;

import com.labloop.entity.LabUser;
import com.labloop.entity.UserRole;
import com.labloop.service.UserService;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Member directory (staff) and "add member" (admin). */
@WebServlet(urlPatterns = {"/members", "/members/*"})
public class MemberServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!staffOnly(resp)) return;
        show(req, resp, new LabUser());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!me.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can add members.");
            return;
        }
        LabUser u = new LabUser();
        try {
            u.setFullName(str(req, "fullName"));
            u.setEmail(str(req, "email"));
            u.setMemberId(str(req, "memberId"));
            u.setDepartment(str(req, "department"));
            UserRole role = enumParam(req, "role", UserRole.class);
            u.setRole(role == null ? UserRole.STUDENT : role);
            userService.create(u, req.getParameter("password"), me.getUserId());
            flashOk(req, u.getFullName() + " added. They can log in with " + u.getEmail() + ".");
            redirect(req, resp, "/members");
        } catch (LabLoopException ex) {
            req.setAttribute("errors", ex.getMessages());
            show(req, resp, u);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, LabUser form) throws ServletException, IOException {
        req.setAttribute("members", userService.findAll());
        req.setAttribute("roles", UserRole.values());
        req.setAttribute("form", form);
        render(req, resp, "members");
    }
}
