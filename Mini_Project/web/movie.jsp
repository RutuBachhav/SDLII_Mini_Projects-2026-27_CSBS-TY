<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.time.LocalDate, com.entity.Movie, com.entity.Show, com.util.Dates, com.util.Html" %>
<%
    Movie movie = (Movie) request.getAttribute("movie");
    List<Show> shows = (List<Show>) request.getAttribute("shows");
    String ctx = request.getContextPath();
    request.setAttribute("pageTitle", movie == null ? "Film" : movie.getTitle());
    request.setAttribute("playTitle", movie == null ? "Cinebook" : movie.getTitle());
    request.setAttribute("playSub", movie == null ? "" : movie.getGenre() + " · " + movie.getLanguage());
    if (movie != null) {
        request.setAttribute("playHref", "#showtimes");
        request.setAttribute("playLabel", movie.isNowShowing() ? "Shows" : "Details");
    }
%>
<jsp:include page="/includes/shell-start.jsp"/>
<% if (movie != null) { %>
<section class="hero">
    <div class="hero-poster" style="background: <%= Html.color(movie.getPosterColor()) %>">
        <strong><%= Html.esc(movie.getTitle().substring(0, 1)) %></strong>
    </div>
    <div>
        <p class="kicker"><%= movie.isNowShowing() ? "Now showing" : "Coming soon" %></p>
        <p class="lede"><%= Html.esc(movie.getDescription()) %></p>
        <div class="meta-row">
            <span class="chip"><%= Html.esc(movie.getCertificate()) %></span>
            <span class="chip"><%= Html.esc(movie.getGenre()) %></span>
            <span class="chip"><%= Html.esc(movie.getLanguage()) %></span>
            <span class="chip"><%= Html.esc(Dates.runtime(movie.getDurationMin())) %></span>
        </div>
    </div>
</section>

<div class="section-head" id="showtimes"><h2>Showtimes</h2></div>
<% if (shows == null || shows.isEmpty()) { %>
<div class="empty"><%= movie.isNowShowing() ? "No upcoming shows for this film." : "Tickets are not open yet." %></div>
<% } else {
    LocalDate last = null;
    for (Show show : shows) {
        if (last == null || !last.equals(show.getShowDate())) {
            last = show.getShowDate();
%>
<h3 class="day-label"><%= Html.esc(Dates.formatDate(show.getShowDate())) %></h3>
<div class="show-list">
<%      } %>
    <article class="show-row">
        <div>
            <strong><%= Html.esc(show.getTheaterName()) %></strong>
            <p class="show-meta"><%= Html.esc(show.getScreenName()) %> · <%= Html.esc(show.getCity()) %></p>
        </div>
        <div class="price"><%= Html.esc(Dates.formatTime(show.getShowTime())) %></div>
        <div>
            <strong><%= Html.esc(Dates.rupees(show.getPrice())) %></strong>
            <p class="show-meta"><%= show.getSeatsLeft() %> seats left</p>
        </div>
        <a class="btn btn-green" href="<%= ctx %>/seats?showId=<%= show.getId() %>">Seats</a>
    </article>
<%      boolean lastOfDay = true;
        int index = shows.indexOf(show);
        if (index + 1 < shows.size() && shows.get(index + 1).getShowDate().equals(show.getShowDate())) {
            lastOfDay = false;
        }
        if (lastOfDay) { %>
</div>
<%      }
    }
} %>
<% } %>
<jsp:include page="/includes/shell-end.jsp"/>
