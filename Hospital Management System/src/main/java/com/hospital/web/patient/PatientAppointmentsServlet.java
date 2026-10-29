package com.hospital.web.patient;

import com.hospital.module.AppUser;
import com.hospital.module.Appointment;
import com.hospital.module.HospitalDAO;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientAppointmentsServlet", urlPatterns = {"/patient/appointments"})
public class PatientAppointmentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getPatient() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<Appointment> all = HospitalDAO.findAppointmentsByPatient(user.getPatient().getId());
        List<Appointment> upcoming = new ArrayList<>();
        List<Appointment> history = new ArrayList<>();
        for (Appointment a : all) {
            switch (a.getStatus()) {
                case PENDING:
                case CONFIRMED:
                    upcoming.add(a);
                    break;
                default:
                    history.add(a);
                    break;
            }
        }
        // Upcoming soonest first.
        upcoming.sort((x, y) -> {
            int c = x.getDate().compareTo(y.getDate());
            return c != 0 ? c : x.getStartTime().compareTo(y.getStartTime());
        });
        req.setAttribute("pageTitle", "My appointments");
        req.setAttribute("upcoming", upcoming);
        req.setAttribute("history", history);
        String notice = req.getParameter("notice");
        if ("booked".equals(notice)) {
            req.setAttribute("notice", "Appointment request sent. You will see it below as Pending until the doctor confirms.");
        } else if ("cancelled".equals(notice)) {
            req.setAttribute("notice", "Appointment cancelled.");
        }
        String error = req.getParameter("error");
        if (error != null && !error.isEmpty()) {
            req.setAttribute("formError", error);
        }
        req.getRequestDispatcher("/WEB-INF/views/patient/appointments.jsp").forward(req, res);
    }
}
