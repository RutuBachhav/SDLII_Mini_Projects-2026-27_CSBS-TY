package com.web;

import com.room.RoomRequestDAO;
import com.room.RoomRequestException;
import com.security.Permission;
import com.security.SessionAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/requestRoom")
public class RequestRoomServlet extends HttpServlet {

    private final RoomRequestDAO requests = new RoomRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/findRoom");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!Access.require(request, response,
                Permission.ROOM_REQUEST_CREATE)) {
            return;
        }

        Long userId = SessionAuth.userId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long roomId = parseId(request.getParameter("roomId"));
        if (roomId == null) {
            response.sendRedirect(request.getContextPath()
                    + "/findRoom?error=invalid");
            return;
        }

        try {
            requests.createRequest(userId, roomId);
            response.sendRedirect(request.getContextPath()
                    + "/myRoomRequests?success=requested");
        } catch (RoomRequestException ex) {
            response.sendRedirect(request.getContextPath()
                    + destination(ex.getCode()));
        } catch (RuntimeException ex) {
            getServletContext().log("Room request failed", ex);
            response.sendRedirect(request.getContextPath()
                    + "/findRoom?error=server");
        }
    }

    private String destination(String code) {
        if ("duplicate".equals(code) || "allocated".equals(code)) {
            return "/myRoomRequests?error=" + code;
        }
        if ("notfound".equals(code) || "unavailable".equals(code)
                || "forbidden".equals(code) || "conflict".equals(code)
                || "inactive".equals(code)) {
            return "/findRoom?error=" + code;
        }
        return "/findRoom?error=server";
    }

    private Long parseId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
