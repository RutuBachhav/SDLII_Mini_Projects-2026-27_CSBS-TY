package com.web;

import com.entity.Room;
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

@WebServlet("/deleteRoom")
public class DeleteRoomServlet extends HttpServlet {

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
                + "/viewRoom?" + query);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.ROOM_DELETE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            java.util.List<Room> rooms = em.createQuery(
                    "SELECT r FROM Room r ORDER BY r.roomId",
                    Room.class
            ).getResultList();

            StringBuilder rows = new StringBuilder();

            if (rooms.isEmpty()) {
                rows.append("<tr><td colspan=\"5\" class=\"empty\">"
                        + "No rooms found. Nothing to delete.</td></tr>");
            }

            for (Room r : rooms) {

                String hostelName = (r.getHostel() != null
                    ? Layout.esc(r.getHostel().getHostelName())
                    : "Not Assigned");

                rows.append("<tr>")
                    .append("<td><span class=\"badge badge-blue\">#").append(r.getRoomId()).append("</span></td>")
                    .append("<td><b>").append(Layout.esc(r.getRoomNumber())).append("</b></td>")
                    .append("<td>").append(hostelName).append("</td>")
                    .append("<td>").append(Layout.statusBadge(r.getStatus())).append("</td>")
                    .append("<td>")
                    .append("<form class=\"inline-form\" action=\"deleteRoom\" method=\"post\" ")
                    .append("onsubmit=\"return confirm(")
                    .append("'Delete room #" + r.getRoomId() + " (" + Layout.esc(r.getRoomNumber()) + ")?');\">")
                    .append("<input type=\"hidden\" name=\"roomId\" value=\"")
                    .append(r.getRoomId()).append("\">")
                    .append(Layout.csrfField())
                    .append("<button class=\"btn btn-danger btn-sm\" type=\"submit\">Delete</button>")
                    .append("</form>")
                    .append("</td>")
                    .append("</tr>");
            }

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Room", "Delete <span>Room</span>", "rooms"));
            out.println(Layout.panel("Select a Room to Delete",
                    rooms.size() + " room(s) registered",
                    "<table class=\"table\">"
                    + "<tr><th>ID</th><th>Room No.</th><th>Hostel</th><th>Status</th><th>Action</th></tr>"
                    + rows.toString()
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a>"
                    + "</div>"));
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Room", "Delete <span>Room</span>", "rooms"));
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

        if (!Access.require(request, response, Permission.ROOM_DELETE)) {
            return;
        }

        boolean returnToDirectory = "viewRoom".equals(
                request.getParameter("returnTo"));
        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            String idParameter = request.getParameter("roomId");

            Long roomId = Long.parseLong(
                    idParameter == null ? "" : idParameter.trim()
            );

            Room room = em.find(Room.class, roomId);

            PrintWriter out = response.getWriter();

            if (room == null) {
                if (returnToDirectory) {
                    redirectToDirectory(request, response,
                            "error=notfound&roomId=" + roomId);
                    return;
                }
                out.println(Layout.open("Delete Room", "Delete <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Room Not Found",
                        "No room exists with ID #" + roomId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteRoom\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            Long activeAllocations = em.createQuery(
                    "SELECT COUNT(a) FROM RoomAllocation a "
                    + "WHERE a.room.roomId = :roomId", Long.class)
                    .setParameter("roomId", roomId)
                    .getSingleResult();

            if (activeAllocations != null && activeAllocations > 0) {
                if (returnToDirectory) {
                    redirectToDirectory(request, response,
                            "error=hasAllocations&roomId=" + roomId);
                    return;
                }
                out.println(Layout.open("Room Has Allocations",
                        "Delete <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Cannot delete this room",
                        "Room <b>#" + roomId + "</b> has <b>"
                        + activeAllocations + "</b> active allocation(s). "
                        + "Release them before deleting the room."));
                out.println("<div class=\"btn-row\">"
                        + "<a class=\"btn btn-primary\" href=\""
                        + request.getContextPath()
                        + "/manageRoomRequests?status=APPROVED\">"
                        + "Manage Allocations</a>"
                        + "<a class=\"btn btn-secondary\" href=\"deleteRoom\">"
                        + "Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            em.getTransaction().begin();

            em.remove(room);

            em.getTransaction().commit();

            if (returnToDirectory) {
                redirectToDirectory(request, response,
                        "success=deleted&roomId=" + roomId);
                return;
            }

            out.println(Layout.open("Room Deleted", "Delete <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Room Deleted Successfully!",
                    "Room <b>#" + roomId + "</b> has been removed."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-primary\" href=\"deleteRoom\">Delete Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            if (returnToDirectory) {
                redirectToDirectory(request, response, "error=invalid");
                return;
            }
            PrintWriter out = response.getWriter();
            out.println(Layout.open("Delete Room", "Delete <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Room ID",
                    "The room ID you submitted is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteRoom\">Back to List</a></div>");
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
            out.println(Layout.open("Delete Room", "Delete <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Something went wrong",
                    "An unexpected error occurred. Please try again."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"deleteRoom\">Back to List</a></div>");
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
