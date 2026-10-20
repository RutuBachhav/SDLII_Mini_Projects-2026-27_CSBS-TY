package com.hospital.web.patient;

import com.hospital.module.AppUser;
import com.hospital.module.AppointmentStatus;
import com.hospital.module.HospitalDAO;
import com.hospital.module.Role;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CancelAppointmentServlet", urlPatterns = {"/patient/cancel"})
public class CancelAppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtil.validCsrf(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request. Please try again.");
            return;
        }
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getPatient() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Long id = parseId(req.getParameter("id"));
        if (id == null) {
            res.sendRedirect(req.getContextPath() + "/patient/appointments");
            return;
        }
        try {
            HospitalDAO.transitionAppointment(id, AppointmentStatus.CANCELLED,
                    Role.PATIENT, user.getPatient().getId(), null, null);
            res.sendRedirect(req.getContextPath() + "/patient/appointments?notice=cancelled");
        } catch (SecurityException e) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only manage your own appointments.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath() + "/patient/appointments?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private static Long parseId(String v) {
        try {
            return v == null ? null : Long.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
