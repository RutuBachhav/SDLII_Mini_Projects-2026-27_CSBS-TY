package com.hospital.web.patient;

import com.hospital.module.AppUser;
import com.hospital.module.Appointment;
import com.hospital.module.HospitalDAO;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientDashboardServlet", urlPatterns = {"/patient/dashboard"})
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getPatient() == null) {
            req.setAttribute("pageTitle", "Something went wrong");
            req.setAttribute("errorTitle", "Profile not found");
            req.setAttribute("errorMessage", "We could not load your patient profile. Please sign in again.");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
            return;
        }
        Long pid = user.getPatient().getId();
        List<Appointment> upcoming = HospitalDAO.findUpcomingByPatient(pid, 5);
        List<Appointment> all = HospitalDAO.findAppointmentsByPatient(pid);
        req.setAttribute("pageTitle", "Dashboard");
        req.setAttribute("patient", user.getPatient());
        req.setAttribute("upcoming", upcoming);
        req.setAttribute("totalCount", all.size());
        req.getRequestDispatcher("/WEB-INF/views/patient/dashboard.jsp").forward(req, res);
    }
}
