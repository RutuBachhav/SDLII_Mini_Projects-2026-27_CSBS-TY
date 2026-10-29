package com.hospital.web.patient;

import com.hospital.module.AppUser;
import com.hospital.module.HospitalDAO;
import com.hospital.module.Patient;
import com.hospital.module.ValidationUtil;
import com.hospital.web.SessionUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientProfileServlet", urlPatterns = {"/patient/profile"})
public class PatientProfileServlet extends HttpServlet {

    private static final Set<String> GENDERS = new HashSet<>(Arrays.asList("Female", "Male", "Other", "Prefer not to say"));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppUser user = HospitalDAO.findUserById(SessionUtil.userId(req));
        if (user == null || user.getPatient() == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("pageTitle", "My profile");
        req.setAttribute("user", user);
        req.setAttribute("patient", user.getPatient());
        if ("saved".equals(req.getParameter("notice"))) {
            req.setAttribute("notice", "Profile updated.");
        }
        req.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(req, res);
    }

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
        String name = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String dobRaw = trim(req.getParameter("dateOfBirth"));
        String gender = trim(req.getParameter("gender"));
        String address = trim(req.getParameter("address"));

        String nameError = null, phoneError = null, dobError = null;
        if (!ValidationUtil.isName(name)) {
            nameError = "Enter your full name (at least 2 characters).";
        }
        if (!phone.isEmpty() && !ValidationUtil.isPhone(phone)) {
            phoneError = "Enter a valid phone number.";
        }
        LocalDate dob = null;
        if (!dobRaw.isEmpty()) {
            dob = ValidationUtil.parseDate(dobRaw);
            if (dob == null || dob.isAfter(LocalDate.now().minusDays(1)) || dob.isBefore(LocalDate.now().minusYears(130))) {
                dobError = "Enter a valid date of birth in the past.";
            }
        }
        if (gender.isEmpty() || !GENDERS.contains(gender)) {
            gender = null;
        }

        Patient p = user.getPatient();
        if (nameError != null || phoneError != null || dobError != null) {
            req.setAttribute("pageTitle", "My profile");
            req.setAttribute("user", user);
            req.setAttribute("patient", p);
            req.setAttribute("nameError", nameError);
            req.setAttribute("phoneError", phoneError);
            req.setAttribute("dobError", dobError);
            req.setAttribute("fullName", name);
            req.setAttribute("phone", phone);
            req.setAttribute("dobRaw", dobRaw);
            req.setAttribute("gender", trim(req.getParameter("gender")));
            req.setAttribute("address", address);
            req.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(req, res);
            return;
        }

        p.setName(name);
        p.setPhone(phone.isEmpty() ? null : phone);
        p.setDateOfBirth(dob);
        p.setGender(gender);
        p.setAddress(address.isEmpty() ? null : address);
        user.setFullName(name);
        HospitalDAO.updatePatient(p);
        // Keep the display name in the session in sync.
        req.getSession(false).setAttribute("uname", name);
        HospitalDAO.updateUserName(user.getId(), name);
        res.sendRedirect(req.getContextPath() + "/patient/profile?notice=saved");
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }
}
