package com.web;

import com.entity.Hostel;
import com.security.Permission;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/updateHostel")
public class UpdateHostelServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {

        try {
            emf = Persistence.createEntityManagerFactory("HostelPU");

            System.out.println(
                "EntityManagerFactory Created Successfully"
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(
                "Error creating EntityManagerFactory", e
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.HOSTEL_UPDATE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            int hostelId = Integer.parseInt(
                request.getParameter("hostelId")
            );

            String hostelName = request.getParameter("hostelName");
            String address = request.getParameter("address");
            int totalFloors = Integer.parseInt(
                request.getParameter("totalFloors").trim()
            );

            PrintWriter out = response.getWriter();

            if (hostelName == null || hostelName.trim().isEmpty()) {
                out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Validation failed", "Hostel Name is required!"));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            if (totalFloors < 1) {
                out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Validation failed", "Total Floors must be at least 1!"));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            Hostel hostel = em.find(Hostel.class, hostelId);

            if (hostel == null) {
                out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel Not Found",
                        "No hostel exists with ID #" + hostelId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            em.getTransaction().begin();

            hostel.setHostelName(hostelName.trim());
            hostel.setAddress(address == null ? "" : address.trim());
            hostel.setTotalFloors(totalFloors);

            em.merge(hostel);

            em.getTransaction().commit();

            out.println(Layout.open("Hostel Updated", "Update <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Hostel Updated Successfully!",
                    "Hostel <b>#" + hostel.getHostelId() + "</b> has been updated."));
            out.println(Layout.panel("Updated Details", "Current saved values",
                    "<table class=\"detail\">"
                    + "<tr><th>Hostel ID</th><td><span class=\"badge badge-blue\">#"
                    + hostel.getHostelId() + "</span></td></tr>"
                    + "<tr><th>Hostel Name</th><td>" + Layout.esc(hostel.getHostelName()) + "</td></tr>"
                    + "<tr><th>Address</th><td>" + Layout.esc(hostel.getAddress()) + "</td></tr>"
                    + "<tr><th>Total Floors</th><td>" + hostel.getTotalFloors() + "</td></tr>"
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-primary\" href=\"updateHostel.html\">Update Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"viewHostel\">View All Hostels</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid input", "Invalid Hostel ID or Total Floors!"));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            if (em != null &&
                em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Something went wrong",
                    "An unexpected error occurred. Please try again."));
            out.println("</div>");
            out.println(Layout.close());

        } finally {

            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void destroy() {

        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
