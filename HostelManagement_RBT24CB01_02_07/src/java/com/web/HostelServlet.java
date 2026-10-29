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

@WebServlet("/saveHostel")
public class HostelServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {

        try {

            emf = Persistence.createEntityManagerFactory("HostelPU");

            System.out.println(
                "EntityManagerFactory Created Successfully"
            );

        } catch (Exception e) {

            System.out.println(
                "Error Creating EntityManagerFactory"
            );

            e.printStackTrace();
        }
    }

    private void errorPage(java.io.PrintWriter out, String message) {
        out.println(Layout.open("Add Hostel", "Add <span>Hostel</span>", "hostels"));
        out.println("<div class=\"narrow\">");
        out.println(Layout.alertErr("Validation failed", Layout.esc(message)));
        out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"addHostel.html\">Try Again</a>"
                + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a></div>");
        out.println("</div>");
        out.println(Layout.close());
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.HOSTEL_CREATE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            String hostelName = request.getParameter("hostelName");
            String address = request.getParameter("address");
            String floorsParam = request.getParameter("totalFloors");

            if (hostelName == null || hostelName.trim().isEmpty()) {
                errorPage(response.getWriter(), "Hostel Name is required!");
                return;
            }

            if (floorsParam == null || floorsParam.trim().isEmpty()) {
                errorPage(response.getWriter(), "Total Floors is required!");
                return;
            }

            int totalFloors = Integer.parseInt(floorsParam.trim());

            if (totalFloors < 1) {
                errorPage(response.getWriter(), "Total Floors must be at least 1!");
                return;
            }

            Hostel hostel = new Hostel();
            hostel.setHostelName(hostelName.trim());
            hostel.setAddress(address == null ? "" : address.trim());
            hostel.setTotalFloors(totalFloors);

            em.getTransaction().begin();

            em.persist(hostel);

            em.getTransaction().commit();

            java.io.PrintWriter out = response.getWriter();
            out.println(Layout.open("Hostel Added", "Add <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Hostel Added Successfully!",
                    "Hostel <b>#" + hostel.getHostelId() + " &mdash; "
                    + Layout.esc(hostel.getHostelName()) + "</b> has been saved."));
            out.println(Layout.panel("Hostel Summary", "Newly created record",
                    "<table class=\"detail\">"
                    + "<tr><th>Hostel ID</th><td><span class=\"badge badge-blue\">#"
                    + hostel.getHostelId() + "</span></td></tr>"
                    + "<tr><th>Hostel Name</th><td>" + Layout.esc(hostel.getHostelName()) + "</td></tr>"
                    + "<tr><th>Address</th><td>" + Layout.esc(hostel.getAddress()) + "</td></tr>"
                    + "<tr><th>Total Floors</th><td>" + hostel.getTotalFloors() + "</td></tr>"
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-primary\" href=\"addHostel.html\">+ Add Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"viewHostel\">View All Hostels</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            errorPage(response.getWriter(), "Invalid Total Floors!");

        } catch (Exception e) {

            if (em != null
                    && em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }

            e.printStackTrace();

            java.io.PrintWriter out = response.getWriter();
            out.println(Layout.open("Error", "Add <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Something went wrong",
                    "An unexpected error occurred. Please try again."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"addHostel.html\">Try Again</a></div>");
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

            System.out.println(
                "EntityManagerFactory Closed"
            );
        }
    }
}
