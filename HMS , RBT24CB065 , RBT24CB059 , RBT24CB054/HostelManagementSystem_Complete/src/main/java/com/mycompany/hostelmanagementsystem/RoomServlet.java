package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "RoomServlet", urlPatterns = {"/RoomServlet"})
public class RoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getSession().getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "new":
                    request.getRequestDispatcher("/admin/room-form.jsp").forward(request, response);
                    break;

                case "edit":
                    int editId = Integer.parseInt(request.getParameter("id"));
                    request.setAttribute("room", roomDAO.getRoomById(editId));
                    request.getRequestDispatcher("/admin/room-form.jsp").forward(request, response);
                    break;

                case "delete":
                    int deleteId = Integer.parseInt(request.getParameter("id"));
                    roomDAO.deleteRoom(deleteId);
                    response.sendRedirect(request.getContextPath() + "/RoomServlet");
                    break;

                default:
                    request.setAttribute("rooms", roomDAO.getAllRooms());
                    request.getRequestDispatcher("/admin/rooms.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("rooms", roomDAO.getAllRooms());
            request.getRequestDispatcher("/admin/rooms.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String idParam = request.getParameter("id");
            String roomNo = request.getParameter("roomNo");
            String roomType = request.getParameter("roomType");
            int capacity = Integer.parseInt(request.getParameter("capacity"));

            if (idParam != null && !idParam.trim().isEmpty()) {
                Room room = roomDAO.getRoomById(Integer.parseInt(idParam));
                if (room == null) throw new RuntimeException("Room not found.");

                room.setRoomNo(roomNo);
                room.setRoomType(roomType);
                room.setCapacity(capacity);
                roomDAO.updateRoom(room);
            } else {
                Room room = new Room();
                room.setRoomNo(roomNo);
                room.setRoomType(roomType);
                room.setCapacity(capacity);
                room.setCurrentOccupancy(0);
                room.setStatus("AVAILABLE");
                roomDAO.addRoom(room);
            }

            response.sendRedirect(request.getContextPath() + "/RoomServlet");

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/admin/room-form.jsp").forward(request, response);
        }
    }
}
