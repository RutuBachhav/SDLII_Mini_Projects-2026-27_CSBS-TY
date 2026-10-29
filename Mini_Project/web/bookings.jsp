<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.time.LocalDateTime, com.entity.Booking, com.util.Dates, com.util.Html" %>
<%
    request.setAttribute("pageTitle", "My tickets");
    request.setAttribute("playTitle", "Your bookings");
    request.setAttribute("playSub", "Confirmed tickets stay here until the show starts");
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/shell-start.jsp"/>
<% if (bookings == null || bookings.isEmpty()) { %>
<div class="empty">You have no tickets yet. Pick a film on the home page.</div>
<% } else { %>
<div class="tickets">
    <% for (Booking booking : bookings) {
        boolean future = booking.getShowDate() != null && LocalDateTime.of(booking.getShowDate(), booking.getShowTime()).isAfter(LocalDateTime.now());
        boolean canCancel = booking.isConfirmed() && future;
    %>
    <article class="ticket <%= booking.isConfirmed() ? "" : "cancelled" %>">
        <div class="ticket-code"><%= Html.esc(booking.getBookingCode()) %></div>
        <div>
            <strong><%= Html.esc(booking.getMovieTitle()) %></strong>
            <p class="show-meta"><%= Html.esc(booking.getTheaterName()) %> · <%= Html.esc(booking.getScreenName()) %></p>
            <p class="show-meta"><%= Html.esc(Dates.formatDate(booking.getShowDate())) %> · <%= Html.esc(Dates.formatTime(booking.getShowTime())) %> · <%= Html.esc(booking.getSeatSummary()) %></p>
        </div>
        <div class="ticket-actions">
            <div>
                <strong><%= Html.esc(Dates.rupees(booking.getTotalAmount())) %></strong>
                <div class="status <%= booking.isConfirmed() ? "ok" : "off" %>"><%= Html.esc(booking.getStatus()) %></div>
            </div>
            <a class="btn small" href="<%= ctx %>/ticket?code=<%= Html.esc(booking.getBookingCode()) %>">Ticket</a>
            <% if (canCancel) { %>
            <form method="post" action="<%= ctx %>/cancel">
                <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                <button class="btn btn-danger small" type="submit">Cancel</button>
            </form>
            <% } %>
        </div>
    </article>
    <% } %>
</div>
<% } %>
<jsp:include page="/includes/shell-end.jsp"/>
