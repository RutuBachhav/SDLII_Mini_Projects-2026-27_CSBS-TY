package com.web;

import com.entity.Room;
import com.entity.RoomRequest;
import com.entity.RoomRequestStatus;
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
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

@WebServlet("/manageRoomRequests")
public class ManageRoomRequestsServlet extends HttpServlet {

    private final RoomRequestDAO requests = new RoomRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response,
                Permission.ROOM_REQUEST_MANAGE)) {
            return;
        }

        String filter = normalizeFilter(request.getParameter("status"));
        RoomRequestStatus status = "ALL".equals(filter)
                ? null : RoomRequestStatus.valueOf(filter);
        List<RoomRequest> rows = requests.findAll(status);
        long pending = requests.countPendingRequests();

        String flashError = RoomRequestSupport.errorMessage(
                request.getParameter("error"));
        String flashSuccess = RoomRequestSupport.successMessage(
                request.getParameter("success"));

        PrintWriter out = response.getWriter();
        out.println(Layout.open("Manage Room Requests",
                "Manage <span>Room Requests</span>", "managerequests"));
        out.println("<div class=\"hero hero-compact\"><h1>Room Request "
                + "Review</h1><p>Approve a pending request to create an "
                + "allocation, reject it, or release an existing allocation. "
                + "Approval rechecks capacity inside a transaction.</p></div>");

        if (flashError != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Room request", flashError));
            out.println("</div>");
        }
        if (flashSuccess != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertOk("Request updated", flashSuccess));
            out.println("</div>");
        }

        out.println("<div class=\"stat-grid\">");
        out.println("<div class=\"stat-card\"><span>Pending review</span>"
                + "<strong>" + pending + "</strong></div>");
        out.println("<div class=\"stat-card\"><span>Shown in this view</span>"
                + "<strong>" + rows.size() + "</strong></div>");
        out.println("</div>");

        out.println("<div class=\"panel\"><div class=\"panel-head\"><div>"
                + "<h3>Requests</h3><p>Only approved allocations consume "
                + "capacity</p></div></div><div class=\"panel-body\">");

        out.println("<div class=\"tabs\">");
        out.println(tab(request, "PENDING", "Pending", filter));
        out.println(tab(request, "APPROVED", "Approved", filter));
        out.println(tab(request, "REJECTED", "Rejected", filter));
        out.println(tab(request, "CANCELLED", "Cancelled", filter));
        out.println(tab(request, "ALL", "All", filter));
        out.println("</div>");

        if (rows.isEmpty()) {
            out.println("<div class=\"empty-state\"><span>&#128203;</span>"
                    + "<h3>No requests in this view</h3>"
                    + "<p>Try another status filter.</p></div>");
        } else {
            out.println("<div class=\"table-wrap\"><table class=\"table\">");
            out.println("<tr><th>Request</th><th>Requester</th><th>Room</th>"
                    + "<th>Fee</th><th>Status</th><th>Requested</th>"
                    + "<th>Decision</th><th>Action</th></tr>");
            for (RoomRequest row : rows) {
                Room room = row.getRoom();
                String hostelName = room.getHostel() == null
                        ? "Not assigned" : room.getHostel().getHostelName();
                String decision = row.getDecidedAt() == null
                        ? "-" : RoomRequestSupport.formatDate(
                                row.getDecidedAt());
                String decidedBy = row.getDecidedBy() == null
                        ? "" : "<small>by "
                        + Layout.esc(row.getDecidedBy().getUsername())
                        + "</small>";

                out.println("<tr>");
                out.println("<td><span class=\"badge badge-blue\">#"
                        + row.getRequestId() + "</span></td>");
                out.println("<td><b>"
                        + Layout.esc(row.getUser().getUsername())
                        + "</b></td>");
                out.println("<td><b>Room " + Layout.esc(room.getRoomNumber())
                        + "</b><br><small>" + Layout.esc(hostelName)
                        + " &middot; capacity " + room.getCapacity()
                        + "</small></td>");
                out.println("<td>&#8377; "
                        + RoomRequestSupport.money(room.getMonthlyFee())
                        + "</td>");
                out.println("<td>"
                        + RoomRequestSupport.requestBadge(row.getStatus())
                        + "</td>");
                out.println("<td>"
                        + RoomRequestSupport.formatDate(row.getRequestedAt())
                        + "</td>");
                out.println("<td>" + decision + "<br>" + decidedBy
                        + "</td>");
                out.println("<td>");
                if (row.getStatus() == RoomRequestStatus.PENDING) {
                    out.println("<div class=\"action-stack\">");
                    out.println(actionForm(request, row, "approve",
                            "Approve &amp; Allocate", "btn btn-primary btn-sm",
                            "Approve this request and create the allocation? "
                            + "Capacity will be checked again."));
                    out.println(actionForm(request, row, "reject", "Reject",
                            "btn btn-danger btn-sm",
                            "Reject this room request?"));
                    out.println("</div>");
                } else if (row.getStatus()
                        == RoomRequestStatus.APPROVED) {
                    out.println(actionForm(request, row, "release",
                            "Release Allocation", "btn btn-secondary btn-sm",
                            "Release this allocation and free the tracked "
                            + "place?"));
                } else {
                    out.println("<span class=\"muted\">-</span>");
                }
                out.println("</td></tr>");
            }
            out.println("</table></div>");
        }

        out.println("</div></div>");
        out.println("<div class=\"btn-row\"><a class=\"btn btn-secondary\" "
                + "href=\"" + request.getContextPath()
                + "/index.jsp\">Dashboard</a></div>");
        out.println(Layout.close());
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!Access.require(request, response,
                Permission.ROOM_REQUEST_MANAGE)) {
            return;
        }

        String action = request.getParameter("action");
        String targetStatus = "release".equals(action)
                ? "APPROVED" : "PENDING";

        if ("approve".equals(action) || "release".equals(action)) {
            if (!Access.require(request, response,
                    Permission.ROOM_ALLOCATION_CONFIRM)) {
                return;
            }
        }

        Long actorId = SessionAuth.userId(request.getSession(false));
        if (actorId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long requestId = parseId(request.getParameter("requestId"));
        if (requestId == null) {
            response.sendRedirect(request.getContextPath()
                    + "/manageRoomRequests?status=" + targetStatus
                    + "&error=invalid");
            return;
        }

        try {
            if ("approve".equals(action)) {
                requests.approve(requestId, actorId);
                response.sendRedirect(request.getContextPath()
                        + "/manageRoomRequests?status=PENDING"
                        + "&success=approved");
            } else if ("reject".equals(action)) {
                requests.reject(requestId, actorId);
                response.sendRedirect(request.getContextPath()
                        + "/manageRoomRequests?status=PENDING"
                        + "&success=rejected");
            } else if ("release".equals(action)) {
                requests.release(requestId, actorId);
                response.sendRedirect(request.getContextPath()
                        + "/manageRoomRequests?status=APPROVED"
                        + "&success=released");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/manageRoomRequests?status=" + targetStatus
                        + "&error=invalid");
            }
        } catch (RoomRequestException ex) {
            response.sendRedirect(request.getContextPath()
                    + "/manageRoomRequests?status=" + targetStatus
                    + "&error=" + ex.getCode());
        } catch (RuntimeException ex) {
            getServletContext().log("Managing room request failed", ex);
            response.sendRedirect(request.getContextPath()
                    + "/manageRoomRequests?status=" + targetStatus
                    + "&error=server");
        }
    }

    private String actionForm(HttpServletRequest request,
                              RoomRequest row,
                              String action,
                              String label,
                              String cssClass,
                              String confirmation) {
        return "<form class=\"inline-form\" action=\""
                + request.getContextPath() + "/manageRoomRequests\" "
                + "method=\"post\" onsubmit=\"return confirm('"
                + Layout.esc(confirmation) + "');\">"
                + Layout.csrfField()
                + "<input type=\"hidden\" name=\"requestId\" value=\""
                + row.getRequestId() + "\">"
                + "<input type=\"hidden\" name=\"action\" value=\""
                + action + "\">"
                + "<button class=\"" + cssClass + "\" type=\"submit\">"
                + label + "</button></form>";
    }

    private String tab(HttpServletRequest request,
                       String value,
                       String label,
                       String current) {
        String active = value.equals(current) ? " active" : "";
        return "<a class=\"tab" + active + "\" href=\""
                + request.getContextPath() + "/manageRoomRequests?status="
                + value + "\">" + label + "</a>";
    }

    private String normalizeFilter(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "PENDING";
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(value)) {
            return "ALL";
        }
        try {
            RoomRequestStatus.valueOf(value);
            return value;
        } catch (IllegalArgumentException ex) {
            return "PENDING";
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
