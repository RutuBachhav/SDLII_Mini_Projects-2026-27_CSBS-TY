<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDateTime, com.entity.Booking, com.util.Dates, com.util.Html" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    request.setAttribute("pageTitle", "Ticket");
    if (booking != null) {
        request.setAttribute("playTitle", booking.getMovieTitle());
        request.setAttribute("playSub", booking.getBookingCode());
        request.setAttribute("playHref", request.getContextPath() + "/bookings");
        request.setAttribute("playLabel", "My tickets");
    }
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/shell-start.jsp"/>
<% if (booking != null) {
    boolean future = LocalDateTime.of(booking.getShowDate(), booking.getShowTime()).isAfter(LocalDateTime.now());
%>
<article class="pass">
    <p class="kicker"><%= booking.isConfirmed() ? "Confirmed" : "Cancelled" %></p>
    <div class="pass-code"><%= Html.esc(booking.getBookingCode()) %></div>
    <div class="pass-grid">
        <div><span>Film</span><strong><%= Html.esc(booking.getMovieTitle()) %></strong></div>
        <div><span>Amount</span><strong><%= Html.esc(Dates.rupees(booking.getTotalAmount())) %></strong></div>
        <div><span>Theatre</span><strong><%= Html.esc(booking.getTheaterName()) %></strong></div>
        <div><span>Screen</span><strong><%= Html.esc(booking.getScreenName()) %></strong></div>
        <div><span>Show</span><strong><%= Html.esc(Dates.formatDate(booking.getShowDate())) %>, <%= Html.esc(Dates.formatTime(booking.getShowTime())) %></strong></div>
        <div><span>Seats</span><strong><%= Html.esc(booking.getSeatSummary()) %></strong></div>
        <div><span>Booked</span><strong><%= Html.esc(Dates.formatDateTime(booking.getBookedAt())) %></strong></div>
        <div><span>Name</span><strong><%= Html.esc(booking.getUserName()) %></strong></div>
    </div>
    <% if (booking.isConfirmed() && future) { %>
    <form method="post" action="<%= ctx %>/cancel" style="margin-top:18px">
        <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
        <button class="btn btn-danger" type="submit">Cancel booking</button>
    </form>
    <% } %>
</article>
<% } %>
<jsp:include page="/includes/shell-end.jsp"/>
