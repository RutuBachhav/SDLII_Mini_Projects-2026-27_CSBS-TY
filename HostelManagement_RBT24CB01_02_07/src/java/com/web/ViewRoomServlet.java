package com.web;

import com.entity.Room;
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

@WebServlet("/viewRoom")
public class ViewRoomServlet extends HttpServlet {

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

    private static String hostelInfo(Room r) {
        if (r.getHostel() == null) {
            return "<span class=\"badge badge-blue\">Not Assigned</span>";
        }
        return "<b>" + Layout.esc(r.getHostel().getHostelName()) + "</b> "
                + "<span class=\"badge badge-amber\">ID #"
                + r.getHostel().getHostelId() + "</span>";
    }

    private Long parsePositiveLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
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

        if (!Access.require(request, response, Permission.ROOM_READ)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            java.util.List<Room> rooms = em.createQuery(
                    "SELECT r FROM Room r ORDER BY r.roomId",
                    Room.class
            ).getResultList();

            java.util.Collection<String> roles =
                    SessionAuth.roles(request.getSession(false));
            String ctx = request.getContextPath();
            boolean canAdd = AuthorizationService.allowsAny(
                    roles, Permission.ROOM_CREATE);
            boolean canUpdate = AuthorizationService.allowsAny(
                    roles, Permission.ROOM_UPDATE);
            boolean canDelete = AuthorizationService.allowsAny(
                    roles, Permission.ROOM_DELETE);
            boolean showActions = canUpdate && canDelete;

            StringBuilder rows = new StringBuilder();

            if (rooms.isEmpty()) {
                rows.append("<tr><td colspan=\"")
                        .append(showActions ? 7 : 6)
                        .append("\" class=\"empty\">"
                        + "No rooms found yet. Click &ldquo;Add Room&rdquo; to create one.</td></tr>");
            }

            for (Room r : rooms) {
                rows.append("<tr>")
                    .append("<td><span class=\"badge badge-blue\">#").append(r.getRoomId()).append("</span></td>")
                    .append("<td><b>").append(Layout.esc(r.getRoomNumber())).append("</b></td>")
                    .append("<td class=\"num\">").append(r.getCapacity()).append("</td>")
                    .append("<td class=\"num\">&#8377; ").append(r.getMonthlyFee()).append("</td>")
                    .append("<td>").append(Layout.statusBadge(r.getStatus())).append("</td>")
                    .append("<td>").append(hostelInfo(r)).append("</td>");

                if (showActions) {
                    rows.append("<td class=\"actions-cell\"><div class=\"row-actions\">");
                    rows.append("<form class=\"inline-form\" action=\"")
                        .append(ctx).append("/getRoomForUpdate\" method=\"post\">")
                        .append(Layout.csrfField())
                        .append("<input type=\"hidden\" name=\"roomId\" value=\"")
                        .append(r.getRoomId()).append("\">")
                        .append("<button class=\"btn btn-primary btn-sm\" type=\"submit\" ")
                        .append("aria-label=\"Update room #").append(r.getRoomId())
                        .append("\" title=\"Update this room\">&#9998; Update</button>")
                        .append("</form>");
                    rows.append("<form class=\"inline-form\" action=\"")
                        .append(ctx).append("/deleteRoom\" method=\"post\" ")
                        .append("onsubmit=\"return confirm('Are you sure you want to delete this room?');\">")
                        .append(Layout.csrfField())
                        .append("<input type=\"hidden\" name=\"roomId\" value=\"")
                        .append(r.getRoomId()).append("\">")
                        .append("<input type=\"hidden\" name=\"returnTo\" value=\"viewRoom\">")
                        .append("<button class=\"btn btn-danger btn-sm\" type=\"submit\" ")
                        .append("aria-label=\"Delete room #").append(r.getRoomId())
                        .append("\" title=\"Delete this room\">&#128465; Delete</button>")
                        .append("</form>");
                    rows.append("</div></td>");
                }
                rows.append("</tr>");
            }

            String flash = "";
            if (canDelete) {
                String success = request.getParameter("success");
                String error = request.getParameter("error");
                Long flashId = parsePositiveLong(request.getParameter("roomId"));
                String idLabel = flashId == null ? "" : "Room <b>#" + flashId + "</b> ";
                if ("deleted".equals(success)) {
                    flash = Layout.alertOk("Room Deleted Successfully!",
                            flashId == null
                                    ? "The room has been removed."
                                    : idLabel + "has been removed.");
                } else if ("hasAllocations".equals(error)) {
                    flash = Layout.alertErr("Cannot Delete Room",
                            flashId == null
                                    ? "This room still has active allocations. Release them before deleting it."
                                    : idLabel + "still has active allocations. Release them before deleting it.");
                } else if ("notfound".equals(error)) {
                    flash = Layout.alertErr("Room Not Found",
                            flashId == null
                                    ? "The selected room no longer exists."
                                    : "No room exists with ID #" + flashId + ".");
                } else if ("invalid".equals(error)) {
                    flash = Layout.alertErr("Invalid Room ID",
                            "The room ID you submitted is not valid.");
                } else if ("server".equals(error)) {
                    flash = Layout.alertErr("Something went wrong",
                            "The room could not be deleted. Please try again.");
                }
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("All Rooms", "All <span>Rooms</span>", "rooms"));
            if (!flash.isEmpty()) {
                out.println(flash);
            }
            out.println(Layout.panel("Room Directory",
                    rooms.size() + " room(s) registered",
                    "<div class=\"table-wrap\"><table class=\"table table-rooms\">"
                    + "<tr><th>ID</th><th>Room No.</th><th>Capacity</th>"
                    + "<th>Monthly Fee</th><th>Status</th><th>Hostel</th>"
                    + (showActions ? "<th>Actions</th>" : "")
                    + "</tr>"
                    + rows.toString()
                    + "</table></div>"
                    + "<div class=\"btn-row\">"
                    + (canAdd ? "<a class=\"btn btn-primary\" href=\"addRoom.jsp\">+ Add Room</a>" : "")
                    + "<a class=\"btn btn-secondary\" href=\"index.jsp\">Dashboard</a>"
                    + "</div>"));
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("All Rooms", "All <span>Rooms</span>", "rooms"));
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

        if (!Access.require(request, response, Permission.ROOM_READ)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            String idParameter = request.getParameter("roomId");

            if (idParameter == null || idParameter.trim().isEmpty()) {
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Room Details", "All <span>Rooms</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Room ID is required",
                        "Please go back to the room list and try again."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            Long roomId = Long.parseLong(idParameter.trim());

            Room room = em.find(Room.class, roomId);

            if (room == null) {
                PrintWriter out = response.getWriter();
                out.println(Layout.open("Room Details", "All <span>Rooms</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Room Not Found",
                        "No room exists with ID #" + roomId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Room Details", "Room <span>Details</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.panel("Room #" + room.getRoomId(),
                    "Room " + Layout.esc(room.getRoomNumber()),
                    "<table class=\"detail\">"
                    + "<tr><th>Room ID</th><td><span class=\"badge badge-blue\">#"
                    + room.getRoomId() + "</span></td></tr>"
                    + "<tr><th>Room Number</th><td>" + Layout.esc(room.getRoomNumber()) + "</td></tr>"
                    + "<tr><th>Capacity</th><td>" + room.getCapacity() + " persons</td></tr>"
                    + "<tr><th>Monthly Fee</th><td>&#8377; " + room.getMonthlyFee() + "</td></tr>"
                    + "<tr><th>Status</th><td>" + Layout.statusBadge(room.getStatus()) + "</td></tr>"
                    + "<tr><th>Hostel</th><td>" + hostelInfo(room) + "</td></tr>"
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a>"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Room Details", "All <span>Rooms</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Room ID",
                    "The room ID you entered is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Room Details", "All <span>Rooms</span>", "rooms"));
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
