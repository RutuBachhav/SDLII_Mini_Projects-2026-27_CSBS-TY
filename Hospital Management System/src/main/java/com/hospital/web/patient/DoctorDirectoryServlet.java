package com.hospital.web.patient;

import com.hospital.module.Doctor;
import com.hospital.module.HospitalDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorDirectoryServlet", urlPatterns = {"/patient/doctors"})
public class DoctorDirectoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String specialty = req.getParameter("specialty");
        String q = req.getParameter("q");
        if (specialty == null) {
            specialty = "";
        }
        if (q == null) {
            q = "";
        }
        String query = q.trim().toLowerCase();

        List<Doctor> all = HospitalDAO.getActiveDoctors();
        TreeSet<String> specialties = new TreeSet<>();
        for (Doctor d : all) {
            if (d.getDepartment() != null) {
                specialties.add(d.getDepartment());
            }
        }
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : all) {
            if (!specialty.isEmpty() && !specialty.equals(d.getDepartment())) {
                continue;
            }
            if (!query.isEmpty() && (d.getName() == null || !d.getName().toLowerCase().contains(query))) {
                continue;
            }
            filtered.add(d);
        }
        req.setAttribute("pageTitle", "Find a doctor");
        req.setAttribute("doctors", filtered);
        req.setAttribute("specialties", specialties);
        req.setAttribute("specialty", specialty);
        req.setAttribute("q", q.trim());
        req.getRequestDispatcher("/WEB-INF/views/patient/doctors.jsp").forward(req, res);
    }
}
