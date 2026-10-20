package com.hospital.module;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AssignServlet", urlPatterns = {"/AssignServlet"})
public class AssignServlet extends HttpServlet {

    private final Random random = new Random();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String patientName = request.getParameter("patientName");
        String doctorIdParam = request.getParameter("doctorId");

        if (patientName != null && !patientName.trim().isEmpty()) {
            Doctor assignedDoctor = null;

            if (doctorIdParam != null && !doctorIdParam.equals("auto")) {
                try {
                    Long doctorId = Long.parseLong(doctorIdParam);
                    assignedDoctor = HospitalDAO.findDoctorById(doctorId);
                } catch (NumberFormatException e) {
                    assignedDoctor = null;
                }
            } else {
                List<Doctor> doctors = HospitalDAO.getAllDoctors();
                if (doctors != null && !doctors.isEmpty()) {
                    assignedDoctor = doctors.get(random.nextInt(doctors.size()));
                }
            }

            if (assignedDoctor != null) {
                Patient patient = new Patient(patientName.trim(), assignedDoctor);
                HospitalDAO.savePatient(patient);
            }
        }

        response.sendRedirect("index.jsp");
    }
}