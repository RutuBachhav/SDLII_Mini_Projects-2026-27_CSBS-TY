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

@WebServlet("/saveRoom")
public class AddRoomServlet extends HttpServlet {

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

    private void errorPage(java.io.PrintWriter out, String heading, String message) {
        out.println(Layout.open("Add Room", "Add <span>Room</span>", "rooms"));
        out.println("<div class=\"narrow\">");
        out.println(Layout.alertErr(heading, Layout.esc(message)));
        out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" href=\"addRoom.html\">Try Again</a>"
                + "<a class=\"btn btn-secondary\" href=\"index.html\">Dashboard</a></div>");
        out.println("</div>");
        out.println(Layout.close());
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.ROOM_CREATE)) {
            return;
        }

        EntityManager em = null;

        try {

            em = emf.createEntityManager();

            String roomNumber = request.getParameter("roomNumber");
            String capacityParam = request.getParameter("capacity");
            String feeParam = request.getParameter("monthlyFee");
            String status = request.getParameter("status");
            String hostelIdParam = request.getParameter("hostelId");

            if (roomNumber == null || roomNumber.trim().isEmpty()) {
                errorPage(response.getWriter(), "Validation failed", "Room Number is required!");
                return;
            }

            int capacity;
            double monthlyFee;
            int hostelId;

            try {
                capacity = Integer.parseInt(capacityParam.trim());
                monthlyFee = Double.parseDouble(feeParam.trim());
                hostelId = Integer.parseInt(hostelIdParam.trim());
            } catch (Exception e) {
                errorPage(response.getWriter(), "Validation failed", "Invalid Capacity, Fee or Hostel ID!");
                return;
            }

            if (capacity < 1) {
                errorPage(response.getWriter(), "Validation failed", "Capacity must be at least 1!");
                return;
            }

            if (monthlyFee <= 0) {
                errorPage(response.getWriter(), "Validation failed", "Monthly Fee must be positive!");
                return;
            }

            if (status == null || status.trim().isEmpty()) {
                status = "AVAILABLE";
            }

            Hostel hostel = em.find(Hostel.class, hostelId);

            if (hostel == null) {
                errorPage(response.getWriter(), "Hostel not found",
                        "No hostel exists with ID #" + hostelId + ". Please create the hostel first.");
                return;
            }

            Room room = new Room();
            room.setRoomNumber(roomNumber.trim());
            room.setCapacity(capacity);
            room.setMonthlyFee(monthlyFee);
            room.setStatus(status.trim().toUpperCase());
            room.setHostel(hostel);

            em.getTransaction().begin();

            em.persist(room);

            em.getTransaction().commit();

            java.io.PrintWriter out = response.getWriter();
            out.println(Layout.open("Room Added", "Add <span>Room</span>", "rooms"));
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Room Saved Successfully!",
                    "Room <b>#" + room.getRoomId() + " &mdash; "
                    + Layout.esc(room.getRoomNumber()) + "</b> has been saved."));
            out.println(Layout.panel("Room Summary", "Newly created record",
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
                    + "<a class=\"btn btn-primary\" href=\"addRoom.html\">+ Add Another</a>"
                    + "<a class=\"btn btn-secondary\" href=\"viewRoom\">View All Rooms</a>"
                    + "</div>"));
            out.println("</div>");
            out.println(Layout.close());

        } catch (Exception e) {

            if (em != null &&
                em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }

            e.printStackTrace();

            errorPage(response.getWriter(), "Something went wrong",
                    "An unexpected error occurred. Please try again.");

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
