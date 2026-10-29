package com.labloop.web;

import com.labloop.service.LabLoopSettingsBean;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** View (everyone) / change (admin) the settings held by the Singleton bean. */
@WebServlet("/settings")
public class SettingsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private LabLoopSettingsBean settings;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("settings", settings);
        render(req, resp, "settings");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!me.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can change settings.");
            return;
        }
        try {
            settings.update(str(req, "institutionName"),
                    intParam(req, "studentMaxHours", settings.getStudentMaxHours()),
                    intParam(req, "facultyMaxHours", settings.getFacultyMaxHours()),
                    intParam(req, "maxAdvanceDays", settings.getMaxAdvanceDays()));
            flashOk(req, "Settings saved - they apply to every user immediately.");
        } catch (LabLoopException ex) {
            flashError(req, ex);
        }
        redirect(req, resp, "/settings");
    }
}
