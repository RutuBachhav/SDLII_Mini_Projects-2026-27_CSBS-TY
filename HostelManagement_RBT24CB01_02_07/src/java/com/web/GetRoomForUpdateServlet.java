package com.web;

import com.entity.Hostel;
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
import java.util.List;

@WebServlet("/getRoomForUpdate")
public class GetRoomForUpdateServlet extends HttpServlet {

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

        if (!Access.require(request, response, Permission.ROOM_UPDATE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            Long roomId = Long.parseLong(
                request.getParameter("roomId")
            );

            Room room = em.find(Room.class, roomId);

            PrintWriter out = response.getWriter();

            if (room == null) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Room Not Found",
                        "No room exists with ID #" + roomId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a>"
                        + "<a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            List<Hostel> hostels = em.createQuery(
                    "SELECT h FROM Hostel h ORDER BY h.hostelId",
                    Hostel.class
            ).getResultList();

            StringBuilder hostelOptions = new StringBuilder();

            for (Hostel h : hostels) {

                hostelOptions.append("<option value=\"")
                    .append(h.getHostelId())
                    .append("\"");

                if (room.getHostel() != null
                        && room.getHostel().getHostelId() == h.getHostelId()) {
                    hostelOptions.append(" selected");
                }

                hostelOptions.append(">")
                    .append(Layout.esc(h.getHostelName()))
                    .append(" (ID #")
                    .append(h.getHostelId())
                    .append(")</option>");
            }

            String selectedAvailable =
                "AVAILABLE".equalsIgnoreCase(room.getStatus()) ? " selected" : "";
            String selectedOccupied =
                "OCCUPIED".equalsIgnoreCase(room.getStatus()) ? " selected" : "";

            out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.panel("Edit Room #" + room.getRoomId(),
                    "Update the fields below and save",
                    "<form action=\"updateRoom\" method=\"post\">"
                    + Layout.csrfField()
                    + "<div class=\"form-group\"><label>Room ID</label>"
                    + "<input type=\"number\" name=\"roomId\" value=\""
                    + room.getRoomId() + "\" readonly></div>"
                    + "<div class=\"form-group\"><label>Room Number</label>"
                    + "<input type=\"text\" name=\"roomNumber\" value=\""
                    + Layout.esc(room.getRoomNumber()) + "\" required></div>"
                    + "<div class=\"form-group\"><label>Capacity</label>"
                    + "<input type=\"number\" name=\"capacity\" min=\"1\" value=\""
                    + room.getCapacity() + "\" required></div>"
                    + "<div class=\"form-group\"><label>Monthly Fee (&#8377;)</label>"
                    + "<input type=\"number\" step=\"0.01\" name=\"monthlyFee\" min=\"0.01\" value=\""
                    + room.getMonthlyFee() + "\" required></div>"
                    + "<div class=\"form-group\"><label>Status</label>"
                    + "<select name=\"status\" required>"
                    + "<option value=\"AVAILABLE\"" + selectedAvailable + ">AVAILABLE</option>"
                    + "<option value=\"OCCUPIED\"" + selectedOccupied + ">OCCUPIED</option>"
                    + "</select></div>"
                    + "<div class=\"form-group\"><label>Hostel</label>"
                    + "<select name=\"hostelId\" required>"
                    + hostelOptions.toString()
                    + "</select></div>"
                    + "<button class=\"btn btn-primary btn-block\" type=\"submit\">"
                    + "Save Changes</button>"
                    + "</form>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-secondary\" href=\"viewRoom\">Back to List</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid Room ID",
                    "The room ID you entered is not valid."));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            e.printStackTrace();

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
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
