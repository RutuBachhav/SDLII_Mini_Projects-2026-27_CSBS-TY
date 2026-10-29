package com.hospital.web.doctor;

import com.hospital.module.AppUser;
import com.hospital.module.AppointmentStatus;
import com.hospital.module.HospitalDAO;
import com.hospital.module.Role;
import com.hospital.module.ValidationUtil;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorAppointmentActionServlet", urlPatterns = {"/doctor/appointments/action"})
public class DoctorAppointmentActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtil.validCsrf(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request. Please try again.");
            return;
        }
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getDoctor() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Long id = parseId(req.getParameter("id"));
        String action = req.getParameter("action");
        String notes = ValidationUtil.clean(req.getParameter("notes"), 2000);
        if (id == null || action == null) {
            res.sendRedirect(req.getContextPath() + "/doctor/appointments");
            return;
        }
        AppointmentStatus target;
        switch (action) {
            case "accept": target = AppointmentStatus.CONFIRMED; break;
            case "reject": target = AppointmentStatus.REJECTED; break;
            case "complete": target = AppointmentStatus.COMPLETED; break;
            case "cancel": target = AppointmentStatus.CANCELLED; break;
            case "note": target = null; break;
            default:
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
                return;
        }
        try {
            if (target == null) {
                // Notes-only update: re-apply current status after ownership check.
                com.hospital.module.Appointment current = HospitalDAO.findAppointmentById(id);
                if (current == null || !current.getDoctor().getId().equals(user.getDoctor().getId())) {
                    throw new SecurityException("You can only manage your own schedule.");
                }
                if (current.getStatus().isTerminal()) {
                    throw new IllegalStateException("Notes cannot be changed once the appointment is closed.");
                }
                if (notes == null || notes.isEmpty()) {
                    throw new IllegalArgumentException("Write a note before saving.");
                }
                HospitalDAO.saveDoctorNote(id, user.getDoctor().getId(), notes);
            } else {
                HospitalDAO.transitionAppointment(id, target, Role.DOCTOR, null, user.getDoctor().getId(), notes);
            }
            res.sendRedirect(req.getContextPath() + "/doctor/appointments?notice=updated");
        } catch (SecurityException e) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only manage your own schedule.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath() + "/doctor/appointments?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
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
