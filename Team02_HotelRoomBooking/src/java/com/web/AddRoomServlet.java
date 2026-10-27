package com.web;

import com.entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/AddRoomServlet")
public class AddRoomServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        double pricePerNight;
        try {
            pricePerNight = Double.parseDouble(request.getParameter("pricePerNight"));
        } catch (NumberFormatException e) {
            styledError(response, "Invalid price!",
                    "Enter the price as digits only, e.g. 2500.");
            return;
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setPricePerNight(pricePerNight);
        room.setAvailable(true); // newly added rooms are always available

        EntityManager em = emf.createEntityManager();

        try {

            em.getTransaction().begin();

            em.persist(room);

            em.getTransaction().commit();

            // Redirect with success message
            response.sendRedirect("addRoom.html?msg=Room%20Added%20Successfully");

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            styledError(response, "Room number already exists!",
                    "Each room number must be unique - pick another one.");

        } finally {

            em.close();
        }
    }

    // Styled error page (same design system, What + How format)
    private void styledError(HttpServletResponse response,
                             String what, String how)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<!DOCTYPE html><html lang='en'><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Room Not Saved - Seaside Grand Hotel</title>"
                + "<link rel='icon' href='data:,'>"
                + "<link rel='stylesheet' href='css/hotel.css'>"
                + "</head><body>"
                + "<header class='topbar'><a class='brand' href='Home.html'>"
                + "Seaside Grand <span>Hotel</span></a>"
                + "<nav><a href='Home.html'>Home</a> "
                + "<a href='AvailableRoomsServlet'>Rooms</a></nav></header>"
                + "<main><div class='result result-err'>"
                + "<span class='badge'>Not saved</span>"
                + "<h1>Room not saved</h1>"
                + "<p><b>What happened:</b> " + what + "</p>"
                + "<p class='fix'><b>How to fix it:</b> " + how + "</p>"
                + "<a class='btn btn-primary btn-block' href='addRoom.html'>"
                + "Try Again</a>"
                + "</div></main>"
                + "<footer class='footer'>&copy; 2026 Seaside Grand Hotel</footer>"
                + "</body></html>");
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
    }
}
