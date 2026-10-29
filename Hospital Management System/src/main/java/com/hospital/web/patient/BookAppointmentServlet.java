package com.hospital.web.patient;

import com.hospital.module.AppUser;
import com.hospital.module.HospitalDAO;
import com.hospital.module.ValidationUtil;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "BookAppointmentServlet", urlPatterns = {"/patient/book"})
public class BookAppointmentServlet extends HttpServlet {

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
        Long doctorId = parseId(req.getParameter("doctorId"));
        LocalDate date = ValidationUtil.parseDate(req.getParameter("date"));
        LocalTime time = ValidationUtil.parseTime(req.getParameter("time"));
        String reason = ValidationUtil.clean(req.getParameter("reason"), 500);

        String base = req.getContextPath() + "/patient/doctor?id=" + doctorId
                + "&date=" + (date == null ? "" : date.toString());
        if (doctorId == null || date == null || time == null) {
            res.sendRedirect(base + "&error=" + URLEncoder.encode(
                    "Choose a valid date and time slot before booking.", "UTF-8"));
            return;
        }
        try {
            HospitalDAO.bookAppointment(user.getPatient().getId(), doctorId, date, time, reason);
            res.sendRedirect(req.getContextPath() + "/patient/appointments?notice=booked");
        } catch (IllegalStateException | IllegalArgumentException e) {
            res.sendRedirect(base + "&error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
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
