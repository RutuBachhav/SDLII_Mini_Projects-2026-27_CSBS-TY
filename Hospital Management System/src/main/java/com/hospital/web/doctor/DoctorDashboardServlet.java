package com.hospital.web.doctor;

import com.hospital.module.AppUser;
import com.hospital.module.Appointment;
import com.hospital.module.HospitalDAO;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorDashboardServlet", urlPatterns = {"/doctor/dashboard"})
public class DoctorDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getDoctor() == null) {
            req.setAttribute("pageTitle", "Something went wrong");
            req.setAttribute("errorTitle", "Profile not found");
            req.setAttribute("errorMessage", "We could not load your doctor profile. Please sign in again.");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
            return;
        }
        Long did = user.getDoctor().getId();
        Map<String, Long> counts = HospitalDAO.doctorCounts(did);
        List<Appointment> upcoming = HospitalDAO.findUpcomingByDoctor(did, 8);
        List<Appointment> today = new ArrayList<>();
        for (Appointment a : upcoming) {
            if (a.getDate().equals(LocalDate.now())) {
                today.add(a);
            }
        }
        req.setAttribute("pageTitle", "Dashboard");
        req.setAttribute("doctor", user.getDoctor());
        req.setAttribute("counts", counts);
        req.setAttribute("today", today);
        req.setAttribute("upcoming", upcoming);
        req.getRequestDispatcher("/WEB-INF/views/doctor/dashboard.jsp").forward(req, res);
    }
}
