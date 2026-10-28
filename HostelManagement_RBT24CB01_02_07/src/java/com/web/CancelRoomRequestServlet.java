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

@WebServlet("/cancelRoomRequest")
public class CancelRoomRequestServlet extends HttpServlet {

    private final RoomRequestDAO requests = new RoomRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/myRoomRequests");
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

        Long requestId = parseId(request.getParameter("requestId"));
        if (requestId == null) {
            response.sendRedirect(request.getContextPath()
                    + "/myRoomRequests?error=invalid");
            return;
        }

        try {
            requests.cancelOwn(requestId, userId);
            response.sendRedirect(request.getContextPath()
                    + "/myRoomRequests?success=cancelled");
        } catch (RoomRequestException ex) {
            response.sendRedirect(request.getContextPath()
                    + "/myRoomRequests?error=" + ex.getCode());
        } catch (RuntimeException ex) {
            getServletContext().log("Cancelling room request failed", ex);
            response.sendRedirect(request.getContextPath()
                    + "/myRoomRequests?error=server");
        }
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
