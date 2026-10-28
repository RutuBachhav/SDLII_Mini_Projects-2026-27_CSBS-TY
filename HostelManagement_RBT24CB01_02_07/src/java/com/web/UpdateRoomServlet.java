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

@WebServlet("/updateRoom")
public class UpdateRoomServlet extends HttpServlet {

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

        if (!Access.require(request, response, Permission.ROOM_UPDATE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            Long roomId = Long.parseLong(
                request.getParameter("roomId").trim()
            );
            String roomNumber = request.getParameter("roomNumber");
            int capacity = Integer.parseInt(
                request.getParameter("capacity").trim()
            );
            double monthlyFee = Double.parseDouble(
                request.getParameter("monthlyFee").trim()
            );
            String status = request.getParameter("status");
            int hostelId = Integer.parseInt(
                request.getParameter("hostelId").trim()
            );

            PrintWriter out = response.getWriter();

            if (roomNumber == null || roomNumber.trim().isEmpty()) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Validation failed", "Room Number is required!"));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            if (capacity < 1) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Validation failed", "Capacity must be at least 1!"));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            if (monthlyFee <= 0) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Validation failed", "Monthly Fee must be positive!"));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            Room room = em.find(Room.class, roomId);

            if (room == null) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Room Not Found",
                        "No room exists with ID #" + roomId + "."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            Hostel hostel = em.find(Hostel.class, hostelId);

            if (hostel == null) {
                out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertErr("Hostel not found",
                        "No hostel exists with ID #" + hostelId + ". Room was not updated."));
                out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
                out.println("</div>");
                out.println(Layout.close());
                return;
            }

            em.getTransaction().begin();

            room.setRoomNumber(roomNumber.trim());
            room.setCapacity(capacity);
            room.setMonthlyFee(monthlyFee);
            room.setStatus(status == null ? "AVAILABLE" : status.trim().toUpperCase());
            room.setHostel(hostel);

            em.merge(room);

            em.getTransaction().commit();

            out.println(Layout.open("Room Updated", "Update <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Room Updated Successfully!",
                    "Room <b>#" + room.getRoomId() + "</b> has been updated."));
            out.println(Layout.panel("Updated Details", "Current saved values",
                    "<table class=\"detail\">"
                    + "<tr><th>Room ID</th><td><span class=\"badge badge-blue\">#"
                    + room.getRoomId() + "</span></td></tr>"
                    + "<tr><th>Room Number</th><td>" + Layout.esc(room.getRoomNumber()) + "</td></tr>"
                    + "<tr><th>Capacity</th><td>" + room.getCapacity() + " persons</td></tr>"
                    + "<tr><th>Monthly Fee</th><td>&#8377; " + room.getMonthlyFee() + "</td></tr>"
                    + "<tr><th>Status</th><td>" + Layout.statusBadge(room.getStatus()) + "</td></tr>"
                    + "<tr><th>Hostel</th><td>" + Layout.esc(hostel.getHostelName())
                    + " <span class=\"badge badge-amber\">ID #" + hostel.getHostelId() + "</span></td></tr>"
                    + "</table>"
                    + "<div class=\"btn-row\">"
                    + "<a class=\"btn btn-primary\" href=\"updateRoom.html\">Update Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"viewRoom\">View All Rooms</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (NumberFormatException e) {

            PrintWriter out = response.getWriter();
            out.println(Layout.open("Update Room", "Update <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Invalid input", "Invalid Room ID, Capacity, Fee or Hostel ID!"));
            out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"updateRoom.html\">Try Again</a></div>");
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            if (em != null &&
                em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }

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
