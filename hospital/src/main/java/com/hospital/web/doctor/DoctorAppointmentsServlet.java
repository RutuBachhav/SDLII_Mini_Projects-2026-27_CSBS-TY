package com.hospital.web.doctor;

import com.hospital.module.AppUser;
import com.hospital.module.Appointment;
import com.hospital.module.AppointmentStatus;
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

@WebServlet(name = "DoctorAppointmentsServlet", urlPatterns = {"/doctor/appointments"})
public class DoctorAppointmentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getDoctor() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String filter = req.getParameter("status");
        List<Appointment> all = HospitalDAO.findAppointmentsByDoctor(user.getDoctor().getId());
        List<Appointment> pending = new ArrayList<>();
        List<Appointment> active = new ArrayList<>();
        List<Appointment> past = new ArrayList<>();
        for (Appointment a : all) {
            if (a.getStatus() == AppointmentStatus.PENDING) {
                pending.add(a);
            }
            if (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED) {
                active.add(a);
            } else {
                past.add(a);
            }
        }
        // Past: most recent first.
        past.sort((x, y) -> {
            int c = y.getDate().compareTo(x.getDate());
            return c != 0 ? c : y.getStartTime().compareTo(x.getStartTime());
        });
        List<Appointment> shown;
        if ("pending".equals(filter)) {
            shown = pending;
        } else if ("past".equals(filter)) {
            shown = past;
        } else {
            filter = "upcoming";
            shown = active;
        }
        req.setAttribute("pageTitle", "Schedule");
        req.setAttribute("shown", shown);
        req.setAttribute("filter", filter);
        req.setAttribute("pendingCount", pending.size());
        req.setAttribute("upcomingCount", active.size());
        req.setAttribute("pastCount", past.size());
        String notice = req.getParameter("notice");
        if ("updated".equals(notice)) {
            req.setAttribute("notice", "Appointment updated.");
        }
        String error = req.getParameter("error");
        if (error != null && !error.isEmpty()) {
            req.setAttribute("formError", error);
        }
        req.getRequestDispatcher("/WEB-INF/views/doctor/appointments.jsp").forward(req, res);
    }
}
