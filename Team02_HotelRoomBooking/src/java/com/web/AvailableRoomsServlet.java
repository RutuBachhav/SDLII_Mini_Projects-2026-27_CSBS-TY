package com.web;

import com.entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/AvailableRoomsServlet")
public class AvailableRoomsServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = emf.createEntityManager();

        try {

            List<Room> rooms = em.createQuery(
                    "SELECT r FROM Room r WHERE r.available = true",
                    Room.class).getResultList();

            request.setAttribute("rooms", rooms);
            request.getRequestDispatcher("availableRooms.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h2 style='color:red;'>Error: "
                    + e.getMessage() + "</h2>");
        } finally {
            em.close();
        }
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
    }
}
