package com.web;

import com.entity.Hostel;
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
import java.util.List;
import java.util.Set;

@WebServlet("/findRoom")
public class FindRoomServlet extends HttpServlet {

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

        Integer hostelId = null;
        Double minFee = null;
        Double maxFee = null;
        Integer minCapacity = null;
        String roomNumber = trimToNull(request.getParameter("roomNumber"));
        String availability = request.getParameter("availability");
        boolean availableOnly = !"all".equalsIgnoreCase(availability);
        String error = null;

        try {
            hostelId = parseInteger(request.getParameter("hostelId"), 1,
                    "Hostel");
            minFee = parseDouble(request.getParameter("minFee"), 0,
                    "Minimum budget");
            maxFee = parseDouble(request.getParameter("maxFee"), 0,
                    "Maximum budget");
            minCapacity = parseInteger(request.getParameter("minCapacity"), 1,
                    "Minimum capacity");
        } catch (IllegalArgumentException ex) {
            error = ex.getMessage();
        }

        if (roomNumber != null && roomNumber.length() > 50) {
            error = "Room number search must be 50 characters or fewer.";
        }
        if (error == null && minFee != null && maxFee != null
                && minFee > maxFee) {
            error = "Minimum budget cannot be greater than maximum budget.";
        }

        List<Hostel> hostels = rooms.findHostels();
        List<AvailableRoom> results = error == null
                ? rooms.search(hostelId, minFee, maxFee, minCapacity,
                        roomNumber, availableOnly)
                : List.of();
        Set<Long> pendingRoomIds = rooms.pendingRoomIds(userId);
        boolean hasAllocation = rooms.hasActiveAllocation(userId);
        String flashError = RoomRequestSupport.errorMessage(
                request.getParameter("error"));
        String flashSuccess = RoomRequestSupport.successMessage(
                request.getParameter("success"));

        PrintWriter out = response.getWriter();
        out.println(Layout.open("Find a Room",
                "Find <span>Your Ideal Room</span>", "roomchoice"));
        out.println("<div class=\"hero hero-compact\">");
        out.println("<h1>Find Your Ideal Room</h1>");
        out.println("<p>Filter genuinely available rooms by hostel, budget, "
                + "capacity, and room number. Requests are reviewed by a "
                + "Warden or Administrator before an allocation is made.</p>");
        out.println("</div>");

        out.println("<div class=\"panel mb\">");
        out.println("<div class=\"panel-head\"><div><h3>&#128269; Room "
                + "Preferences</h3><p>Every filter is applied to the live "
                + "database</p></div></div>");
        out.println("<div class=\"panel-body\">");
        out.println("<form method=\"get\" action=\"" + request.getContextPath()
                + "/findRoom\" class=\"filter-grid\">");

        out.println("<div class=\"form-group\"><label for=\"hostelId\">"
                + "Hostel</label><select id=\"hostelId\" name=\"hostelId\">");
        out.println("<option value=\"\">All hostels</option>");
        for (Hostel hostel : hostels) {
            String selected = hostelId != null
                    && hostelId == hostel.getHostelId() ? " selected" : "";
            out.println("<option value=\"" + hostel.getHostelId() + "\""
                    + selected + ">" + Layout.esc(hostel.getHostelName())
                    + "</option>");
        }
        out.println("</select></div>");

        out.println("<div class=\"form-group\"><label for=\"minFee\">"
                + "Minimum budget (&#8377;)</label>"
                + "<input id=\"minFee\" type=\"number\" name=\"minFee\" "
                + "min=\"0\" step=\"0.01\" placeholder=\"e.g. 2000\" value=\""
                + (minFee == null ? "" : minFee) + "\"></div>");
        out.println("<div class=\"form-group\"><label for=\"maxFee\">"
                + "Maximum budget (&#8377;)</label>"
                + "<input id=\"maxFee\" type=\"number\" name=\"maxFee\" "
                + "min=\"0\" step=\"0.01\" placeholder=\"e.g. 8000\" value=\""
                + (maxFee == null ? "" : maxFee) + "\"></div>");

        out.println("<div class=\"form-group\"><label for=\"minCapacity\">"
                + "Minimum capacity (persons)</label>"
                + "<input id=\"minCapacity\" type=\"number\" "
                + "name=\"minCapacity\" min=\"1\" placeholder=\"e.g. 2\" "
                + "value=\"" + (minCapacity == null ? "" : minCapacity)
                + "\"></div>");
        out.println("<div class=\"form-group\"><label for=\"roomNumber\">"
                + "Room number contains</label>"
                + "<input id=\"roomNumber\" type=\"text\" name=\"roomNumber\" "
                + "maxlength=\"50\" placeholder=\"e.g. A-1\" value=\""
                + Layout.esc(roomNumber == null ? "" : roomNumber)
                + "\"></div>");

        out.println("<div class=\"form-group\"><label for=\"availability\">"
                + "Availability</label><select id=\"availability\" "
                + "name=\"availability\">"
                + "<option value=\"available\""
                + (availableOnly ? " selected" : "")
                + ">Available only</option>"
                + "<option value=\"all\""
                + (availableOnly ? "" : " selected")
                + ">All rooms</option></select></div>");

        out.println("<div class=\"filter-actions\">"
                + "<button class=\"btn btn-primary\" type=\"submit\">"
                + "Search Rooms</button>"
                + "<a class=\"btn btn-secondary\" href=\""
                + request.getContextPath() + "/findRoom\">Reset</a></div>");
        out.println("</form>");
        out.println("<p class=\"hint\">Availability combines the room's "
                + "AVAILABLE status with approved allocations. Pending "
                + "requests do not reserve capacity. Existing occupancy that "
                + "predates this feature is not tracked here.</p>");
        out.println("</div></div>");

        if (error != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Check your filters",
                    Layout.esc(error)));
            out.println("</div>");
        } else if (flashError != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Room request", flashError));
            out.println("</div>");
        }

        if (flashSuccess != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Request updated", flashSuccess));
            out.println("</div>");
        }

        if (hasAllocation) {
            out.println("<div class=\"alert alert-info\">"
                    + "<span class=\"ico\">&#8505;</span><div>"
                    + "<h4>You already have an approved allocation</h4>"
                    + "<p>You can browse rooms, but a new request cannot be "
                    + "approved while your allocation is active. Contact a "
                    + "Warden or Administrator if it needs to be released.</p>"
                    + "</div></div>");
        }

        if (error == null && results.isEmpty()) {
            out.println("<div class=\"panel\"><div class=\"panel-body\">");
            out.println("<div class=\"empty-state\"><span>&#128269;</span>"
                    + "<h3>No rooms match these preferences</h3>"
                    + "<p>Try increasing your budget, reducing the minimum "
                    + "capacity, choosing another hostel, or showing all "
                    + "rooms.</p>"
                    + "<a class=\"btn btn-secondary\" href=\""
                    + request.getContextPath() + "/findRoom\">Reset filters</a>"
                    + "</div>");
            out.println("</div></div>");
        } else if (error == null) {
            out.println("<div class=\"result-head\"><div><h3>Matching rooms"
                    + "</h3><p>Compare capacity, fee, and current "
                    + "availability.</p></div>"
                    + "<span class=\"badge badge-blue\">" + results.size()
                    + " found</span></div>");
            out.println("<div class=\"room-grid\">");
            for (AvailableRoom availableRoom : results) {
                renderRoomCard(out, request, availableRoom, pendingRoomIds,
                        hasAllocation);
            }
            out.println("</div>");
        }

        out.println("<div class=\"btn-row\">");
        out.println("<a class=\"btn btn-secondary\" href=\""
                + request.getContextPath() + "/myRoomRequests\">"
                + "My Room Requests</a>");
        out.println("<a class=\"btn btn-secondary\" href=\""
                + request.getContextPath() + "/index.jsp\">Dashboard</a>");
        out.println("</div>");
        out.println(Layout.close());
    }

    private void renderRoomCard(PrintWriter out,
                                HttpServletRequest request,
                                AvailableRoom availableRoom,
                                Set<Long> pendingRoomIds,
                                boolean hasAllocation) {

        Room room = availableRoom.getRoom();
        boolean pending = pendingRoomIds.contains(room.getRoomId());
        boolean requestable = availableRoom.isAvailable()
                && !pending && !hasAllocation;
        String hostelName = room.getHostel() == null
                ? "Not assigned" : room.getHostel().getHostelName();

        out.println("<article class=\"room-card\">");
        out.println("<div class=\"room-card-head\"><div>");
        out.println("<span class=\"badge badge-blue\">#"
                + room.getRoomId() + "</span>");
        out.println("<h4>Room " + Layout.esc(room.getRoomNumber()) + "</h4>");
        out.println("<p>" + Layout.esc(hostelName) + "</p>");
        out.println("</div>");
        out.println("<div class=\"room-fee\">&#8377; "
                + RoomRequestSupport.money(room.getMonthlyFee())
                + "<small>/month</small></div>");
        out.println("</div>");

        out.println("<div class=\"room-meta\">");
        out.println("<span>Capacity <b>" + room.getCapacity()
                + "</b> persons</span>");
        out.println("<span>Approved allocations <b>"
                + availableRoom.getActiveAllocations() + "</b></span>");
        out.println("</div>");

        out.println("<div class=\"room-badges\">");
        out.println(Layout.statusBadge(room.getStatus()));
        out.println(RoomRequestSupport.availabilityBadge(availableRoom));
        if (pending) {
            out.println("<span class=\"badge badge-amber\">REQUEST "
                    + "PENDING</span>");
        }
        out.println("</div>");

        out.println("<div class=\"room-card-body\">");
        if (!availableRoom.isAvailable()) {
            out.println("<p>This room is not currently open for new "
                    + "requests.</p>");
        } else if (hasAllocation) {
            out.println("<p>You already hold an approved allocation.</p>");
        } else if (pending) {
            out.println("<p>Your request for this room is waiting for "
                    + "review.</p>");
        } else {
            out.println("<p>Submit a request and a Warden or Administrator "
                    + "can confirm the allocation.</p>");
        }
        out.println("</div>");

        out.println("<div class=\"room-actions\">");
        out.println("<a class=\"btn btn-secondary btn-sm\" href=\""
                + request.getContextPath() + "/roomDetails?roomId="
                + room.getRoomId() + "\">View Details</a>");
        if (requestable) {
            out.println("<form class=\"inline-form\" action=\""
                    + request.getContextPath() + "/requestRoom\" "
                    + "method=\"post\">");
            out.println(Layout.csrfField());
            out.println("<input type=\"hidden\" name=\"roomId\" value=\""
                    + room.getRoomId() + "\">");
            out.println("<button class=\"btn btn-primary btn-sm\" "
                    + "type=\"submit\">Request This Room</button>");
            out.println("</form>");
        } else if (pending) {
            out.println("<a class=\"btn btn-secondary btn-sm\" href=\""
                    + request.getContextPath() + "/myRoomRequests\">"
                    + "View My Requests</a>");
        } else {
            out.println("<button class=\"btn btn-secondary btn-sm\" "
                    + "type=\"button\" disabled>Not Available</button>");
        }
        out.println("</div></article>");
    }

    private Integer parseInteger(String value, int min, String label) {
        String clean = trimToNull(value);
        if (clean == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(clean);
            if (parsed < min) {
                throw new IllegalArgumentException(
                        label + " must be at least " + min + ".");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    label + " must be a whole number.");
        }
    }

    private Double parseDouble(String value, double min, String label) {
        String clean = trimToNull(value);
        if (clean == null) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(clean);
            if (parsed < min) {
                throw new IllegalArgumentException(
                        label + " must be at least " + min + ".");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    label + " must be a number.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}
