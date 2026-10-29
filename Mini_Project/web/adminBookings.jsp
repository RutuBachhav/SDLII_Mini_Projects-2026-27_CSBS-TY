<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.time.LocalDateTime, com.entity.Booking, com.util.Dates, com.util.Html" %>
<%
    request.setAttribute("pageTitle", "Sales");
    request.setAttribute("playTitle", "All bookings");
    request.setAttribute("playSub", "Every ticket sold from this studio");
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/shell-start.jsp"/>
<div class="table-wrap">
    <table>
        <thead>
        <tr><th>Code</th><th>Member</th><th>Film</th><th>Show</th><th>Seats</th><th>Amount</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
        <% if (bookings == null || bookings.isEmpty()) { %>
        <tr><td colspan="8">No bookings yet.</td></tr>
        <% } else {
            for (Booking booking : bookings) {
                boolean future = LocalDateTime.of(booking.getShowDate(), booking.getShowTime()).isAfter(LocalDateTime.now());
        %>
        <tr>
            <td><a href="<%= ctx %>/ticket?code=<%= Html.esc(booking.getBookingCode()) %>"><strong><%= Html.esc(booking.getBookingCode()) %></strong></a></td>
            <td><%= Html.esc(booking.getUserName()) %></td>
            <td><%= Html.esc(booking.getMovieTitle()) %></td>
            <td><%= Html.esc(Dates.formatDate(booking.getShowDate())) %><div class="muted"><%= Html.esc(Dates.formatTime(booking.getShowTime())) %> · <%= Html.esc(booking.getTheaterName()) %></div></td>
            <td><%= Html.esc(booking.getSeatSummary()) %></td>
            <td><%= Html.esc(Dates.rupees(booking.getTotalAmount())) %></td>
            <td class="status <%= booking.isConfirmed() ? "ok" : "off" %>"><%= Html.esc(booking.getStatus()) %></td>
            <td>
                <% if (booking.isConfirmed() && future) { %>
                <form method="post" action="<%= ctx %>/cancel">
                    <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                    <input type="hidden" name="from" value="admin">
                    <button class="btn btn-danger small" type="submit">Cancel</button>
                </form>
                <% } %>
            </td>
        </tr>
        <%  }
        } %>
        </tbody>
    </table>
</div>
<jsp:include page="/includes/shell-end.jsp"/>
