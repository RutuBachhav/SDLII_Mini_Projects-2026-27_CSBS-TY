package com.web;

import com.entity.Room;
import com.entity.RoomRequest;
import com.entity.RoomRequestStatus;
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

@WebServlet("/myRoomRequests")
public class MyRoomRequestsServlet extends HttpServlet {

    private final RoomRequestDAO requests = new RoomRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        if (!Access.require(request, response,
                Permission.ROOM_REQUEST_READ_OWN)) {
            return;
        }

        Long userId = SessionAuth.userId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<RoomRequest> rows = requests.findForUser(userId);
        int pending = 0;
        int approved = 0;
        for (RoomRequest row : rows) {
            if (row.getStatus() == RoomRequestStatus.PENDING) {
                pending++;
            } else if (row.getStatus() == RoomRequestStatus.APPROVED) {
                approved++;
            }
        }

        String flashError = RoomRequestSupport.errorMessage(
                request.getParameter("error"));
        String flashSuccess = RoomRequestSupport.successMessage(
                request.getParameter("success"));

        PrintWriter out = response.getWriter();
        out.println(Layout.open("My Room Requests",
                "My <span>Room Requests</span>", "myrequests"));
        out.println("<div class=\"hero hero-compact\"><h1>My Room Requests"
                + "</h1><p>Track pending preferences and approved allocations. "
                + "Pending requests do not reserve capacity.</p></div>");

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
        out.println("<div class=\"stat-card\"><span>Total requests</span>"
                + "<strong>" + rows.size() + "</strong></div>");
        out.println("<div class=\"stat-card\"><span>Pending</span><strong>"
                + pending + "</strong></div>");
        out.println("<div class=\"stat-card\"><span>Approved</span><strong>"
                + approved + "</strong></div>");
        out.println("</div>");

        out.println("<div class=\"panel\"><div class=\"panel-head\"><div>"
                + "<h3>Request history</h3><p>Only your own requests are "
                + "shown</p></div>"
                + "<a class=\"btn btn-primary btn-sm\" href=\""
                + request.getContextPath() + "/findRoom\">Find a Room</a>"
                + "</div><div class=\"panel-body\">");

        if (rows.isEmpty()) {
            out.println("<div class=\"empty-state\"><span>&#128221;</span>"
                    + "<h3>No room requests yet</h3>"
                    + "<p>Search for an available room and submit your first "
                    + "preference.</p>"
                    + "<a class=\"btn btn-primary\" href=\""
                    + request.getContextPath() + "/findRoom\">Find Your "
                    + "Ideal Room</a></div>");
        } else {
            out.println("<div class=\"table-wrap\"><table class=\"table\">");
            out.println("<tr><th>Request</th><th>Room</th><th>Fee</th>"
                    + "<th>Requested</th><th>Status</th><th>Decision</th>"
                    + "<th>Action</th></tr>");
            for (RoomRequest row : rows) {
                Room room = row.getRoom();
                String hostelName = room.getHostel() == null
                        ? "Not assigned" : room.getHostel().getHostelName();
                String decision = row.getDecidedAt() == null
                        ? "-" : RoomRequestSupport.formatDate(
                                row.getDecidedAt());
                String decisionBy = row.getDecidedBy() == null
                        ? "" : "<small>by "
                        + Layout.esc(row.getDecidedBy().getUsername())
                        + "</small>";

                out.println("<tr>");
                out.println("<td><span class=\"badge badge-blue\">#"
                        + row.getRequestId() + "</span></td>");
                out.println("<td><b>Room " + Layout.esc(room.getRoomNumber())
                        + "</b><br><small>" + Layout.esc(hostelName)
                        + "</small></td>");
                out.println("<td>&#8377; "
                        + RoomRequestSupport.money(room.getMonthlyFee())
                        + "</td>");
                out.println("<td>"
                        + RoomRequestSupport.formatDate(row.getRequestedAt())
                        + "</td>");
                out.println("<td>"
                        + RoomRequestSupport.requestBadge(row.getStatus())
                        + "</td>");
                out.println("<td>" + decision + "<br>" + decisionBy
                        + "</td>");
                out.println("<td>");
                if (row.getStatus() == RoomRequestStatus.PENDING) {
                    out.println("<form class=\"inline-form\" action=\""
                            + request.getContextPath()
                            + "/cancelRoomRequest\" method=\"post\" "
                            + "onsubmit=\"return confirm('Cancel this pending "
                            + "room request?');\">");
                    out.println(Layout.csrfField());
                    out.println("<input type=\"hidden\" name=\"requestId\" "
                            + "value=\"" + row.getRequestId() + "\">");
                    out.println("<button class=\"btn btn-danger btn-sm\" "
                            + "type=\"submit\">Cancel</button>");
                    out.println("</form>");
                } else if (row.getStatus()
                        == RoomRequestStatus.APPROVED) {
                    out.println("<span class=\"badge badge-green\">"
                            + "ALLOCATED</span>");
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
}
