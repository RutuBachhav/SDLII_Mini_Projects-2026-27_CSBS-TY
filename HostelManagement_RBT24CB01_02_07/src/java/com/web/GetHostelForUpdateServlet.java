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

@WebServlet("/getHostelForUpdate")
public class GetHostelForUpdateServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {

        try {
            emf = Persistence.createEntityManagerFactory("HostelPU");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
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

            Hostel hostel = em.find(Hostel.class, hostelId);

            PrintWriter out = response.getWriter();

            if (hostel == null) {
                out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel Not Found",
                        "No hostel exists with ID #" + hostelId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a>"
                        + "<a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.panel("Edit Hostel #" + hostel.getHostelId(),
                    "Update the fields below and save",
                    "<form action=\"updateHostel\" method=\"post\">"
                    + Layout.csrfField()
                    + "<div class=\"form-group\"><label>Hostel ID</label>"
                    + "<input type=\"number\" name=\"hostelId\" value=\""
                    + hostel.getHostelId() + "\" readonly></div>"
                    + "<div class=\"form-group\"><label>Hostel Name</label>"
                    + "<input type=\"text\" name=\"hostelName\" value=\""
                    + Layout.esc(hostel.getHostelName()) + "\" required></div>"
                    + "<div class=\"form-group\"><label>Address</label>"
                    + "<input type=\"text\" name=\"address\" value=\""
                    + Layout.esc(hostel.getAddress()) + "\"></div>"
                    + "<div class=\"form-group\"><label>Total Floors</label>"
                    + "<input type=\"number\" name=\"totalFloors\" min=\"1\" value=\""
                    + hostel.getTotalFloors() + "\" required></div>"
                    + "<button class=\"btn btn-primary btn-block\" type=\"submit\">"
                    + "Save Changes</button>"
                    + "</form>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Hostel ID",
                    "The hostel ID you entered is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateHostel.html\">Try Again</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

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
