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

@WebServlet("/deleteHostel")
public class DeleteHostelServlet extends HttpServlet {

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

    private void redirectToDirectory(HttpServletRequest request,
            HttpServletResponse response, String query)
            throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/viewHostel?" + query);
    }

    private long roomCount(EntityManager em, int hostelId) {
        // Count with a query so the guard is always evaluated fresh.
        return em.createQuery(
                "SELECT COUNT(r) FROM Room r "
                + "WHERE r.hostel.hostelId = :hid",
                Long.class
        ).setParameter("hid", hostelId).getSingleResult();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.HOSTEL_DELETE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            java.util.List<Hostel> hostels = em.createQuery(
                    "SELECT h FROM Hostel h ORDER BY h.hostelId",
                    Hostel.class
            ).getResultList();

            StringBuilder rows = new StringBuilder();

            if (hostels.isEmpty()) {
                rows.append("<tr><td colspan=\"5\" class=\"empty\">"
                        + "No hostels found. Nothing to delete.</td></tr>");
            }

            for (Hostel h : hostels) {

                long count = roomCount(em, h.getHostelId());

                String roomBadge = count > 0
                        ? "<span class=\"badge badge-red\">" + count + " room(s)</span>"
                        : "<span class=\"badge badge-green\">empty</span>";

                rows.append("<tr>")
                    .append("<td><span class=\"badge badge-blue\">#").append(h.getHostelId()).append("</span></td>")
                    .append("<td><b>").append(Layout.esc(h.getHostelName())).append("</b><br>")
                    .append("<span style=\"color:#6b7280;font-size:12.5px;\">")
                    .append(Layout.esc(h.getAddress())).append("</span></td>")
                    .append("<td>").append(roomBadge).append("</td>")
                    .append("<td>")
                    .append("<form class=\"inline-form\" action=\"deleteHostel\" method=\"post\" ")
                    .append("onsubmit=\"return confirm(")
                    .append("'Delete hostel #" + h.getHostelId() + " (" + Layout.esc(h.getHostelName()) + ")?');\">")
                    .append("<input type=\"hidden\" name=\"hostelId\" value=\"")
                    .append(h.getHostelId()).append("\">")
                    .append(Layout.csrfField())
                    .append("<button class=\"btn btn-danger btn-sm\" type=\"submit\">Delete</button>")
                    .append("</form>")
                    .append("</td>")
                    .append("</tr>");
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
            out.println(Layout.panel("Select a Hostel to Delete",
                    "Hostels with rooms cannot be deleted",
                    "<table class=\"table\">"
                    + "<tr><th>ID</th><th>Hostel</th><th>Rooms</th><th>Action</th></tr>"
                    + rows.toString()
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a>"
                    + "</div>"));
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
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
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.HOSTEL_DELETE)) {
            return;
        }

        boolean returnToDirectory = "viewHostel".equals(
                request.getParameter("returnTo"));
        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            int hostelId = Integer.parseInt(
                request.getParameter("hostelId")
            );

            Hostel hostel = em.find(Hostel.class, hostelId);

            if (hostel == null) {
                if (returnToDirectory) {
                    redirectToDirectory(request, response,
                            "error=notfound&hostelId=" + hostelId);
                    return;
                }
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel Not Found",
                        "No hostel exists with ID #" + hostelId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteHostel\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            long count = roomCount(em, hostelId);

            if (count > 0) {
                if (returnToDirectory) {
                    redirectToDirectory(request, response,
                            "error=hasRooms&hostelId=" + hostelId);
                    return;
                }
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Cannot Delete Hostel",
                        "Hostel <b>#" + hostelId + " &mdash; "
                        + Layout.esc(hostel.getHostelName())
                        + "</b> still has <b>" + count + " room(s)</b> assigned. "
                        + "Delete its rooms first."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteHostel\">Back to List</a>"
                        + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            em.getTransaction().begin();

            Hostel managed = em.merge(hostel);

            em.remove(managed);

            em.getTransaction().commit();

            if (returnToDirectory) {
                redirectToDirectory(request, response,
                        "success=deleted&hostelId=" + hostelId);
                return;
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Hostel Deleted", "Delete <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Hostel Deleted Successfully!",
                    "Hostel <b>#" + hostelId + "</b> has been removed."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-primary\" href=\"deleteHostel\">Delete Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            if (returnToDirectory) {
                redirectToDirectory(request, response, "error=invalid");
                return;
            }
            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Hostel ID",
                    "The hostel ID you submitted is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteHostel\">Back to List</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            if (em != null
                    && em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }

            e.printStackTrace();

            if (returnToDirectory) {
                redirectToDirectory(request, response, "error=server");
                return;
            }
            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Hostel", "Delete <span>Hostel</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Something went wrong",
                    "An unexpected error occurred. Please try again."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteHostel\">Back to List</a></div>");
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
