package com.hospital.web.doctor;

import com.hospital.module.AppUser;
import com.hospital.module.Doctor;
import com.hospital.module.HospitalDAO;
import com.hospital.module.ValidationUtil;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorProfileServlet", urlPatterns = {"/doctor/profile"})
public class DoctorProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getDoctor() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("pageTitle", "My profile");
        req.setAttribute("doctor", user.getDoctor());
        if ("saved".equals(req.getParameter("notice"))) {
            req.setAttribute("notice", "Profile and availability updated.");
        }
        req.getRequestDispatcher("/WEB-INF/views/doctor/profile.jsp").forward(req, res);
    }

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
        Doctor d = user.getDoctor();
        String phone = trim(req.getParameter("phone"));
        String qualification = trim(req.getParameter("qualification"));
        String bio = trim(req.getParameter("bio"));
        String feeRaw = trim(req.getParameter("consultationFee"));
        String workStart = trim(req.getParameter("workStart"));
        String workEnd = trim(req.getParameter("workEnd"));
        String slotRaw = trim(req.getParameter("slotMinutes"));
        boolean active = req.getParameter("active") != null;

        String phoneError = null, feeError = null, hoursError = null, slotError = null;
        if (!phone.isEmpty() && !ValidationUtil.isPhone(phone)) {
            phoneError = "Enter a valid phone number.";
        }
        Integer fee = null;
        if (!feeRaw.isEmpty()) {
            try {
                fee = Integer.valueOf(feeRaw);
                if (fee < 0 || fee > 100000) {
                    feeError = "Enter a fee between 0 and 100000.";
                    fee = null;
                }
            } catch (NumberFormatException e) {
                feeError = "Enter a whole number for the fee.";
            }
        }
        java.time.LocalTime ws = ValidationUtil.parseTime(workStart);
        java.time.LocalTime we = ValidationUtil.parseTime(workEnd);
        if (ws == null || we == null || !ws.isBefore(we)) {
            hoursError = "Working hours must be valid times like 09:00, with start before end.";
        }
        int slotMinutes = 30;
        try {
            slotMinutes = Integer.parseInt(slotRaw);
            if (slotMinutes != 15 && slotMinutes != 20 && slotMinutes != 30 && slotMinutes != 45 && slotMinutes != 60) {
                slotError = "Choose 15, 20, 30, 45 or 60 minutes.";
            }
        } catch (NumberFormatException e) {
            slotError = "Choose 15, 20, 30, 45 or 60 minutes.";
        }

        if (phoneError != null || feeError != null || hoursError != null || slotError != null) {
            req.setAttribute("pageTitle", "My profile");
            req.setAttribute("doctor", d);
            req.setAttribute("phoneError", phoneError);
            req.setAttribute("feeError", feeError);
            req.setAttribute("hoursError", hoursError);
            req.setAttribute("slotError", slotError);
            req.setAttribute("phone", phone);
            req.setAttribute("qualification", qualification);
            req.setAttribute("bio", bio);
            req.setAttribute("feeRaw", feeRaw);
            req.setAttribute("workStart", workStart);
            req.setAttribute("workEnd", workEnd);
            req.setAttribute("slotRaw", slotRaw);
            req.setAttribute("activeChecked", active);
            req.getRequestDispatcher("/WEB-INF/views/doctor/profile.jsp").forward(req, res);
            return;
        }

        d.setPhone(phone.isEmpty() ? null : phone);
        d.setQualification(qualification.isEmpty() ? null : qualification);
        d.setBio(bio.isEmpty() ? null : bio);
        d.setConsultationFee(fee);
        d.setWorkStart(workStart);
        d.setWorkEnd(workEnd);
        d.setSlotMinutes(slotMinutes);
        d.setActive(active);
        HospitalDAO.updateDoctor(d);
        res.sendRedirect(req.getContextPath() + "/doctor/profile?notice=saved");
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }
}
