package com.web;

import com.entity.Room;
import com.room.AvailableRoom;
import com.room.RoomRequestDAO;
import com.security.Permission;
import com.security.SessionAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/roomDetails")
public class RoomDetailsServlet extends HttpServlet {

    private final RoomRequestDAO rooms = new RoomRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response, Permission.ROOM_SEARCH)) {
            return;
        }

        Long userId = SessionAuth.userId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long roomId = parseId(request.getParameter("roomId"));
        if (roomId == null) {
            renderNotFound(request, response, null);
            return;
        }

        AvailableRoom availableRoom = rooms.findAvailableRoom(roomId);
        if (availableRoom == null) {
            renderNotFound(request, response, roomId);
            return;
        }

        Room room = availableRoom.getRoom();
        boolean pending = rooms.pendingRoomIds(userId).contains(roomId);
        boolean hasAllocation = rooms.hasActiveAllocation(userId);
        boolean requestable = availableRoom.isAvailable()
                && !pending && !hasAllocation;
        String hostelName = room.getHostel() == null
                ? "Not assigned" : room.getHostel().getHostelName();

        PrintWriter out = response.getWriter();
        out.println(Layout.open("Room " + room.getRoomNumber(),
                "Room <span>Details</span>", "roomchoice"));

        out.println("<div class=\"page-back\"><a href=\""
                + request.getContextPath() + "/findRoom\">&larr; Back to "
                + "room search</a></div>");

        out.println("<div class=\"detail-layout\">");
        out.println("<div class=\"detail-main\">");
        out.println("<div class=\"panel mb\"><div class=\"panel-body\">");
        out.println("<div class=\"details-head\"><div>");
        out.println("<span class=\"badge badge-blue\">Room #"
                + room.getRoomId() + "</span>");
        out.println("<h1 class=\"details-title\">Room "
                + Layout.esc(room.getRoomNumber()) + "</h1>");
        out.println("<p class=\"details-sub\">" + Layout.esc(hostelName)
                + "</p></div>");
        out.println("<div class=\"room-fee room-fee-lg\">&#8377; "
                + RoomRequestSupport.money(room.getMonthlyFee())
                + "<small>/month</small></div>");
        out.println("</div>");
        out.println("<div class=\"room-badges\">");
        out.println(Layout.statusBadge(room.getStatus()));
        out.println(RoomRequestSupport.availabilityBadge(availableRoom));
        if (pending) {
            out.println("<span class=\"badge badge-amber\">REQUEST "
                    + "PENDING</span>");
        }
        out.println("</div>");

        out.println("<div class=\"detail-stats\">");
        out.println("<div class=\"detail-stat\"><span>Capacity</span>"
                + "<strong>" + room.getCapacity() + " persons</strong></div>");
        out.println("<div class=\"detail-stat\"><span>Approved "
                + "allocations</span><strong>"
                + availableRoom.getActiveAllocations()
                + "</strong><small>Tracked by this system</small></div>");
        out.println("<div class=\"detail-stat\"><span>Monthly fee</span>"
                + "<strong>&#8377; "
                + RoomRequestSupport.money(room.getMonthlyFee())
                + "</strong></div>");
        out.println("<div class=\"detail-stat\"><span>Hostel</span>"
                + "<strong>" + Layout.esc(hostelName) + "</strong></div>");
        out.println("</div>");

        out.println("<p class=\"hint\">Availability is calculated from the "
                + "room status and approved allocations. Pending requests do "
                + "not consume capacity. Occupancy that predates this feature "
                + "is not represented by allocation records.</p>");
        out.println("</div></div>");

        out.println("<div class=\"panel\"><div class=\"panel-head\"><div>"
                + "<h3>How room selection works</h3><p>Interest is not the "
                + "same as a confirmed allocation</p></div></div>");
        out.println("<div class=\"panel-body\">");
        out.println("<ol class=\"step-list\">"
                + "<li><b>Submit a request.</b> Your preference is stored as "
                + "PENDING and does not reserve a bed.</li>"
                + "<li><b>Warden or Administrator review.</b> An authorized "
                + "user checks the request and current capacity.</li>"
                + "<li><b>Approval creates the allocation.</b> Capacity is "
                + "checked again in a transaction to prevent overbooking.</li>"
                + "<li><b>Release or cancellation.</b> Releasing an approved "
                + "allocation frees the tracked place again.</li></ol>");
        out.println("</div></div>");
        out.println("</div>");

        out.println("<aside class=\"detail-side\">");
        out.println("<div class=\"panel\"><div class=\"panel-body\">");
        out.println("<h3 class=\"side-title\">Request this room</h3>");
        if (pending) {
            out.println("<p>You already have a pending request for this "
                    + "room.</p>");
            out.println("<a class=\"btn btn-secondary btn-block\" href=\""
                    + request.getContextPath() + "/myRoomRequests\">"
                    + "View My Requests</a>");
        } else if (hasAllocation) {
            out.println("<p>You already hold an approved room allocation, so "
                    + "another request cannot be approved.</p>");
            out.println("<a class=\"btn btn-secondary btn-block\" href=\""
                    + request.getContextPath() + "/myRoomRequests\">"
                    + "View My Requests</a>");
        } else if (!availableRoom.isAvailable()) {
            out.println("<p>This room is not currently open for new "
                    + "requests.</p>");
            out.println("<button class=\"btn btn-secondary btn-block\" "
                    + "type=\"button\" disabled>Not Available</button>");
        } else {
            out.println("<p>Send a preference to the hostel team. A Warden "
                    + "or Administrator must approve it before you are "
                    + "allocated.</p>");
            out.println("<form action=\"" + request.getContextPath()
                    + "/requestRoom\" method=\"post\">");
            out.println(Layout.csrfField());
            out.println("<input type=\"hidden\" name=\"roomId\" value=\""
                    + room.getRoomId() + "\">");
            out.println("<button class=\"btn btn-primary btn-block\" "
                    + "type=\"submit\">Submit Room Request</button>");
            out.println("</form>");
        }
        out.println("<a class=\"btn btn-secondary btn-block\" href=\""
                + request.getContextPath() + "/findRoom\">Compare More "
                + "Rooms</a>");
        out.println("</div></div>");
        out.println("</aside>");
        out.println("</div>");

        out.println(Layout.close());
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

    private void renderNotFound(HttpServletRequest request,
                                HttpServletResponse response,
                                Long roomId) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        PrintWriter out = response.getWriter();
        out.println(Layout.open("Room Not Found",
                "Room <span>Not Found</span>", "roomchoice"));
        out.println("<div class=\"narrow\">");
        out.println(Layout.alertErr("Room not found",
                roomId == null
                        ? "The room ID is missing or invalid."
                        : "No room exists with ID #" + roomId + "."));
        out.println("<div class=\"btn-row\"><a class=\"btn btn-primary\" "
                + "href=\"" + request.getContextPath() + "/findRoom\">"
                + "Back to Room Search</a></div>");
        out.println("</div>");
        out.println(Layout.close());
    }
}
