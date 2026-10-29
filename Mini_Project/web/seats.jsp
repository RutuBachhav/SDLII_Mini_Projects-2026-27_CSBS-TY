<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Set, java.time.LocalDateTime, com.entity.Show, com.entity.User, com.util.Auth, com.util.Dates, com.util.Html, com.util.Seats" %>
<%
    Show show = (Show) request.getAttribute("show");
    Set<String> taken = (Set<String>) request.getAttribute("taken");
    User me = Auth.current(request);
    boolean started = show != null && !LocalDateTime.of(show.getShowDate(), show.getShowTime()).isAfter(LocalDateTime.now());
    String ctx = request.getContextPath();
    request.setAttribute("pageTitle", "Seats");
    if (show != null) {
        request.setAttribute("playTitle", show.getMovieTitle());
        request.setAttribute("playSub", show.getTheaterName() + " · " + Dates.formatDate(show.getShowDate()) + " · " + Dates.formatTime(show.getShowTime()));
    }
%>
<jsp:include page="/includes/shell-start.jsp"/>
<% if (show != null) { %>
<div class="seat-layout">
    <section>
        <div class="screen-wrap">
            <span>SCREEN</span>
            <div class="screen-glow"></div>
        </div>
        <form id="seatForm" method="post" action="<%= ctx %>/book" data-price="<%= show.getPrice() %>">
            <input type="hidden" name="showId" value="<%= show.getId() %>">
            <% for (int row = 0; row < show.getTotalRows(); row++) { %>
            <div class="seat-row">
                <span class="row-label"><%= (char) ('A' + row) %></span>
                <% for (int seat = 1; seat <= show.getSeatsPerRow(); seat++) {
                    if (seat == (show.getSeatsPerRow() / 2) + 1) { %>
                <span class="aisle"></span>
                <% }
                    String label = Seats.label(row, seat);
                    boolean booked = taken != null && taken.contains(label);
                %>
                <label class="seat">
                    <input type="checkbox" name="seats" value="<%= label %>" <%= booked ? "disabled" : "" %>>
                    <span><%= seat %></span>
                </label>
                <% } %>
            </div>
            <% } %>
        </form>
        <div class="legend">
            <span><i class="swatch free"></i>Open</span>
            <span><i class="swatch mine"></i>Selected</span>
            <span><i class="swatch gone"></i>Booked</span>
        </div>
    </section>
    <aside class="summary">
        <p class="kicker"><%= Html.esc(show.getCity()) %></p>
        <h3><%= Html.esc(show.getMovieTitle()) %></h3>
        <p class="muted"><%= Html.esc(show.getTheaterName()) %> · <%= Html.esc(show.getScreenName()) %></p>
        <p class="muted"><%= Html.esc(Dates.formatDate(show.getShowDate())) %> · <%= Html.esc(Dates.formatTime(show.getShowTime())) %></p>
        <dl>
            <dt>Ticket</dt><dd><%= Html.esc(Dates.rupees(show.getPrice())) %></dd>
            <dt>Seats</dt><dd id="seatCount">0</dd>
            <dt>Total</dt><dd id="seatTotal">₹0</dd>
        </dl>
        <% if (started) { %>
        <p class="banner err">This show has already started.</p>
        <% } else if (me == null) { %>
        <a class="btn btn-green" href="<%= ctx %>/login">Sign in to book</a>
        <% } else { %>
        <button class="btn btn-green" id="bookBtn" type="submit" form="seatForm" disabled>Book</button>
        <% } %>
        <p class="hint">Up to 8 seats in one booking.</p>
    </aside>
</div>
<script src="<%= ctx %>/js/seats.js"></script>
<% } %>
<jsp:include page="/includes/shell-end.jsp"/>
