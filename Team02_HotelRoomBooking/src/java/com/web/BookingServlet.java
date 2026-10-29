package com.web;

import com.entity.Booking;
import com.entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/BookingServlet")
public class BookingServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        try {
            // Explicitly register the MySQL driver (classic JDBC step)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ServletException("MySQL driver not found!", e);
        }
        emf = Persistence.createEntityManagerFactory("HotelBookingPU");
    }

    // GET : load the selected room and show the booking form
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long roomId;
        try {
            roomId = Long.parseLong(request.getParameter("roomId"));
        } catch (NumberFormatException e) {
            showError(response, "Invalid Room ID!");
            return;
        }

        EntityManager em = emf.createEntityManager();

        try {

            Room room = em.find(Room.class, roomId);

            if (room == null) {
                showError(response, "Room Not Found!");
                return;
            }

            if (!room.isAvailable()) {
                showError(response,
                        "Sorry, Room " + room.getRoomNumber()
                        + " is already booked!");
                return;
            }

            request.setAttribute("room", room);
            request.getRequestDispatcher("bookRoom.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            showError(response, e.getMessage());
        } finally {
            em.close();
        }
    }

    // POST : validate input, create booking, mark room unavailable
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String customerName = request.getParameter("customerName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String checkIn = request.getParameter("checkInDate");
        String checkOut = request.getParameter("checkOutDate");

        // 1. required-field validation
        if (isEmpty(customerName) || isEmpty(email) || isEmpty(phone)
                || isEmpty(checkIn) || isEmpty(checkOut)) {
            showError(response, "All fields are required!");
            return;
        }

        // 2. guest-count validation
        int guests;
        try {
            guests = Integer.parseInt(request.getParameter("numberOfGuests"));
            if (guests <= 0) {
                showError(response,
                        "Number of guests must be at least 1!");
                return;
            }
        } catch (NumberFormatException e) {
            showError(response, "Invalid number of guests!");
            return;
        }

        // 3. date validation : checkout must be after checkin
        try {
            LocalDate in = LocalDate.parse(checkIn);
            LocalDate outDate = LocalDate.parse(checkOut);
            if (!outDate.isAfter(in)) {
                showError(response,
                        "Check-out date must be after check-in date!",
                        "Pick a check-out day later than your check-in day.");
                return;
            }
        } catch (Exception e) {
            showError(response, "Invalid dates! Use the date picker.");
            return;
        }

        // 4. roomId validation
        Long roomId;
        try {
            roomId = Long.parseLong(request.getParameter("roomId"));
        } catch (NumberFormatException e) {
            showError(response, "Invalid Room ID!");
            return;
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            // 5. load room and re-verify availability
            Room room = em.find(Room.class, roomId);

            if (room == null) {
                tx.rollback();
                showError(response, "Room Not Found!");
                return;
            }

            if (!room.isAvailable()) {
                tx.rollback();
                showError(response,
                        "Room " + room.getRoomNumber()
                        + " was just booked by someone else!",
                        "Choose another room from the available list.");
                return;
            }

            // 6. create + persist booking
            Booking booking = new Booking();
            booking.setCustomerName(customerName);
            booking.setEmail(email);
            booking.setPhone(phone);
            booking.setCheckInDate(checkIn);
            booking.setCheckOutDate(checkOut);
            booking.setNumberOfGuests(guests);
            booking.setRoom(room);

            em.persist(booking);

            // 7. mark room unavailable
            room.setAvailable(false);

            tx.commit();

            // 8. success page (same design system as the JSPs)
            out.println("<!DOCTYPE html><html lang='en'><head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<title>Booking Confirmed - Seaside Grand Hotel</title>"
                    + "<link rel='icon' href='data:,'>"
                    + "<link rel='stylesheet' href='css/hotel.css'>"
                    + "</head><body>"
                    + "<header class='topbar'><a class='brand' href='Home.html'>"
                    + "Seaside Grand <span>Hotel</span></a>"
                    + "<nav><a href='Home.html'>Home</a> "
                    + "<a href='AvailableRoomsServlet'>Rooms</a></nav></header>"
                    + "<main><div class='result result-ok'>"
                    + "<span class='badge'>Confirmed</span>"
                    + "<h1>You're booked, " + escape(customerName) + "!</h1>"
                    + "<p>Your room is held for these dates. Show this page at check-in.</p>"
                    + "<dl class='kv'>"
                    + "<div><dt>Room</dt><dd>" + room.getRoomNumber()
                    + " (" + room.getRoomType() + ")</dd></div>"
                    + "<div><dt>Check-in</dt><dd>" + checkIn + "</dd></div>"
                    + "<div><dt>Check-out</dt><dd>" + checkOut + "</dd></div>"
                    + "<div><dt>Guests</dt><dd>" + guests + "</dd></div>"
                    + "</dl>"
                    + "<a class='btn btn-primary btn-block' href='Home.html'>Back to Home</a>"
                    + "</div></main>"
                    + "<footer class='footer'>&copy; 2026 Seaside Grand Hotel</footer>"
                    + "</body></html>");

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
            showError(response, e.getMessage());

        } finally {

            em.close();
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void showError(HttpServletResponse response, String msg)
            throws IOException {
        showError(response, msg, "Please check the details and try again.");
    }

    private void showError(HttpServletResponse response,
                           String what, String how)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html lang='en'><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Booking Failed - Seaside Grand Hotel</title>"
                + "<link rel='icon' href='data:,'>"
                + "<link rel='stylesheet' href='css/hotel.css'>"
                + "</head><body>"
                + "<header class='topbar'><a class='brand' href='Home.html'>"
                + "Seaside Grand <span>Hotel</span></a>"
                + "<nav><a href='Home.html'>Home</a> "
                + "<a href='AvailableRoomsServlet'>Rooms</a></nav></header>"
                + "<main><div class='result result-err'>"
                + "<span class='badge'>Not booked</span>"
                + "<h1>Booking failed</h1>"
                + "<p><b>What happened:</b> " + escape(what) + "</p>"
                + "<p class='fix'><b>How to fix it:</b> " + escape(how) + "</p>"
                + "<a class='btn btn-primary btn-block' "
                + "href='AvailableRoomsServlet'>View Available Rooms</a>"
                + "<a class='btn btn-quiet btn-block' href='Home.html'>"
                + "Back to Home</a>"
                + "</div></main>"
                + "<footer class='footer'>&copy; 2026 Seaside Grand Hotel</footer>"
                + "</body></html>");
    }

    // Minimal HTML escaping for reflected values (viva: prevents broken pages)
    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
    }
}
