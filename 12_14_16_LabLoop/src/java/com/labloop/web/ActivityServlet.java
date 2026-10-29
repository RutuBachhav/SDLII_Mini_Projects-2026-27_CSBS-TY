package com.labloop.web;

import com.labloop.service.ActivityLogService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Audit trail written by the CDI event observer. */
@WebServlet("/activity")
public class ActivityServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private ActivityLogService activityLog;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!staffOnly(resp)) return;
        req.setAttribute("rows", activityLog.recent(200));
        render(req, resp, "activity");
    }
}
