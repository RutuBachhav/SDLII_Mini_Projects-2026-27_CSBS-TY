package com.hospital.web.patient;

import com.hospital.module.Doctor;
import com.hospital.module.HospitalDAO;
import com.hospital.module.ValidationUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorDetailServlet", urlPatterns = {"/patient/doctor"})
public class DoctorDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Long id = parseId(req.getParameter("id"));
        Doctor doctor = id == null ? null : HospitalDAO.findDoctorById(id);
        if (doctor == null || !doctor.isActive()) {
            req.setAttribute("pageTitle", "Doctor not found");
            req.setAttribute("errorTitle", "Doctor not found");
            req.setAttribute("errorMessage", "This doctor is not available. Return to the directory to choose another.");
            req.setAttribute("backLink", req.getContextPath() + "/patient/doctors");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
            return;
        }
        LocalDate date = ValidationUtil.parseDate(req.getParameter("date"));
        if (date == null || date.isBefore(LocalDate.now())
                || date.isAfter(LocalDate.now().plusDays(HospitalDAO.MAX_BOOK_AHEAD_DAYS))) {
            date = LocalDate.now();
        }
        // Next 14 selectable days for the date control.
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            days.add(LocalDate.now().plusDays(i));
        }
        List<LocalTime> slots = HospitalDAO.availableSlots(doctor.getId(), date);

        req.setAttribute("pageTitle", doctor.getName());
        req.setAttribute("doctor", doctor);
        req.setAttribute("selectedDate", date);
        req.setAttribute("days", days);
        req.setAttribute("slots", slots);
        String booked = req.getParameter("booked");
        String failed = req.getParameter("error");
        if (booked != null) {
            req.setAttribute("notice", "Request sent. The clinic will confirm your appointment shortly.");
        } else if (failed != null) {
            req.setAttribute("formError", failed);
        }
        req.getRequestDispatcher("/WEB-INF/views/patient/doctor-detail.jsp").forward(req, res);
    }

    private static Long parseId(String v) {
        try {
            return v == null ? null : Long.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
