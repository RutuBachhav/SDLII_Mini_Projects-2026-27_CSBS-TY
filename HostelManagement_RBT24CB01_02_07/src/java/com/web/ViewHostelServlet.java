package com.web;

import com.entity.Hostel;
import com.security.AuthorizationService;
import com.security.Permission;
import com.security.SessionAuth;
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

@WebServlet("/viewHostel")
public class ViewHostelServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {

        try {
            emf = Persistence.createEntityManagerFactory("HostelPU");

            System.out.println("EntityManagerFactory Created Successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(
                "Error creating EntityManagerFactory", e
            );
        }
    }

    private long roomCount(EntityManager em, int hostelId) {
        // Count with a query so the number is always read fresh from the DB.
        return em.createQuery(
                "SELECT COUNT(r) FROM Room r "
                + "WHERE r.hostel.hostelId = :hid",
                Long.class
        ).setParameter("hid", hostelId).getSingleResult();
    }

    private Integer parsePositive(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.HOSTEL_READ)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            java.util.List<Hostel> hostels = em.createQuery(
                    "SELECT h FROM Hostel h ORDER BY h.hostelId",
                    Hostel.class
            ).getResultList();

            String ctx = request.getContextPath();
            boolean canAdd = AuthorizationService.allowsAny(
                    SessionAuth.roles(request.getSession(false)),
                    Permission.HOSTEL_CREATE);
            boolean canUpdate = AuthorizationService.allowsAny(
                    SessionAuth.roles(request.getSession(false)),
                    Permission.HOSTEL_UPDATE);
            boolean canDelete = AuthorizationService.allowsAny(
                    SessionAuth.roles(request.getSession(false)),
                    Permission.HOSTEL_DELETE);
            boolean showActions = canUpdate || canDelete;

            StringBuilder rows = new StringBuilder();

            if (hostels.isEmpty()) {
                rows.append("<tr><td colspan=\"")
                        .append(showActions ? 6 : 5)
                        .append("\" class=\"empty\">"
                        + "No hostels found yet. Click &ldquo;Add Hostel&rdquo; to create one.</td></tr>");
            }

            for (Hostel h : hostels) {
                rows.append("<tr>")
                    .append("<td><span class=\"badge badge-blue\">#").append(h.getHostelId()).append("</span></td>")
                    .append("<td><b>").append(Layout.esc(h.getHostelName())).append("</b></td>")
                    .append("<td>").append(Layout.esc(h.getAddress())).append("</td>")
                    .append("<td class=\"num\">").append(h.getTotalFloors()).append("</td>")
                    .append("<td class=\"num\"><span class=\"badge badge-amber\">")
                    .append(roomCount(em, h.getHostelId())).append(" rooms</span></td>");

                if (showActions) {
                    rows.append("<td class=\"actions-cell\"><div class=\"row-actions\">");
                    if (canUpdate) {
                        rows.append("<form class=\"inline-form\" action=\"")
                            .append(ctx).append("/getHostelForUpdate\" method=\"post\">")
                            .append(Layout.csrfField())
                            .append("<input type=\"hidden\" name=\"hostelId\" value=\"")
                            .append(h.getHostelId()).append("\">")
                            .append("<button class=\"btn btn-primary btn-sm\" type=\"submit\" ")
                            .append("aria-label=\"Update hostel #").append(h.getHostelId())
                            .append("\" title=\"Update this hostel\">&#9998; Update</button>")
                            .append("</form>");
                    }
                    if (canDelete) {
                        rows.append("<form class=\"inline-form\" action=\"")
                            .append(ctx).append("/deleteHostel\" method=\"post\" ")
                            .append("onsubmit=\"return confirm('Are you sure you want to delete this hostel?');\">")
                            .append(Layout.csrfField())
                            .append("<input type=\"hidden\" name=\"hostelId\" value=\"")
                            .append(h.getHostelId()).append("\">")
                            .append("<input type=\"hidden\" name=\"returnTo\" value=\"viewHostel\">")
                            .append("<button class=\"btn btn-danger btn-sm\" type=\"submit\" ")
                            .append("aria-label=\"Delete hostel #").append(h.getHostelId())
                            .append("\" title=\"Delete this hostel\">&#128465; Delete</button>")
                            .append("</form>");
                    }
                    rows.append("</div></td>");
                }
                rows.append("</tr>");
            }

            String flash = "";
            if (canDelete) {
                String success = request.getParameter("success");
                String error = request.getParameter("error");
                Integer flashId = parsePositive(request.getParameter("hostelId"));
                String idLabel = flashId == null ? "" : "Hostel <b>#" + flashId + "</b> ";
                if ("deleted".equals(success)) {
                    flash = Layout.alertOk("Hostel Deleted Successfully!",
                            flashId == null
                                    ? "The hostel has been removed."
                                    : idLabel + "has been removed.");
                } else if ("hasRooms".equals(error)) {
                    flash = Layout.alertErr("Cannot Delete Hostel",
                            flashId == null
                                    ? "This hostel still has rooms assigned. Remove its rooms first."
                                    : idLabel + "still has rooms assigned. Remove its rooms first.");
                } else if ("notfound".equals(error)) {
                    flash = Layout.alertErr("Hostel Not Found",
                            flashId == null
                                    ? "The selected hostel no longer exists."
                                    : "No hostel exists with ID #" + flashId + ".");
                } else if ("invalid".equals(error)) {
                    flash = Layout.alertErr("Invalid Hostel ID",
                            "The hostel ID you submitted is not valid.");
                } else if ("server".equals(error)) {
                    flash = Layout.alertErr("Something went wrong",
                            "The hostel could not be deleted. Please try again.");
                }
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("All Hostels", "All <span>Hostels</span>", "hostels"));
            if (!flash.isEmpty()) {
                out.println(flash);
            }
            out.println(Layout.panel("Hostel Directory",
                    hostels.size() + " hostel(s) registered",
                    "<div class=\"table-wrap\"><table class=\"table table-hostels\">"
                    + "<tr><th>ID</th><th>Hostel Name</th><th>Address</th>"
                    + "<th>Floors</th><th>Rooms</th>"
                    + (showActions ? "<th>Actions</th>" : "")
                    + "</tr>"
                    + rows.toString()
                    + "</table></div>"
                    + "<div class=\"btn-row\">"
                    + (canAdd ? "<a class=\"btn btn-primary\" href=\"addHostel.jsp\">+ Add Hostel</a>" : "")
                    + "<a class=\"btn btn-secondary\" href=\"index.jsp\">Dashboard</a>"
                    + "</div>"));
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("All Hostels", "All <span>Hostels</span>", "hostels"));
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

        if (!Access.require(request, response, Permission.HOSTEL_READ)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            String idParameter = request.getParameter("hostelId");

            if (idParameter == null || idParameter.trim().isEmpty()) {
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Hostel Details", "All <span>Hostels</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel ID is required",
                        "Please go back to the hostel list and try again."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            int hostelId = Integer.parseInt(idParameter.trim());

            Hostel hostel = em.find(Hostel.class, hostelId);

            if (hostel == null) {
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Hostel Details", "All <span>Hostels</span>", "hostels"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel Not Found",
                        "No hostel exists with ID #" + hostelId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            long count = roomCount(em, hostelId);

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Hostel Details", "Hostel <span>Details</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.panel(Layout.esc(hostel.getHostelName()),
                    "Hostel #" + hostel.getHostelId(),
                    "<table class=\"detail\">"
                    + "<tr><th>Hostel ID</th><td><span class=\"badge badge-blue\">#"
                    + hostel.getHostelId() + "</span></td></tr>"
                    + "<tr><th>Hostel Name</th><td>" + Layout.esc(hostel.getHostelName()) + "</td></tr>"
                    + "<tr><th>Address</th><td>" + Layout.esc(hostel.getAddress()) + "</td></tr>"
                    + "<tr><th>Total Floors</th><td>" + hostel.getTotalFloors() + "</td></tr>"
                    + "<tr><th>Rooms Assigned</th><td><span class=\"badge badge-amber\">"
                    + count + " room(s)</span></td></tr>"
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a>"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Hostel Details", "All <span>Hostels</span>", "hostels"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Hostel ID",
                    "The hostel ID you entered is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewHostel\">Back to List</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Hostel Details", "All <span>Hostels</span>", "hostels"));
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
