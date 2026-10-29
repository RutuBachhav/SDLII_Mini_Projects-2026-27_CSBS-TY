<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.entity.Movie, com.util.Dates, com.util.Html" %>
<%
    request.setAttribute("pageTitle", "Find your next film");
    request.setAttribute("playTitle", "Now showing");
    request.setAttribute("playSub", "Pick a film, then a seat");
    List<Movie> movies = (List<Movie>) request.getAttribute("movies");
    List<String> genres = (List<String>) request.getAttribute("genres");
    String activeGenre = request.getParameter("genre");
    if (activeGenre == null) {
        activeGenre = "";
    }
    String ctx = request.getContextPath();
    Integer nowCount = (Integer) request.getAttribute("nowCount");
    Integer theaterCount = (Integer) request.getAttribute("theaterCount");
    Integer todayCount = (Integer) request.getAttribute("todayCount");
%>
<jsp:include page="/includes/shell-start.jsp"/>

<% if (nowCount != null) { %>
<section class="stats">
    <div class="stat"><strong><%= nowCount %></strong><span>Films playing</span></div>
    <div class="stat"><strong><%= theaterCount %></strong><span>Theatres</span></div>
    <div class="stat"><strong><%= todayCount %></strong><span>Shows today</span></div>
</section>
<% } %>

<div class="chips">
    <a class="chip <%= activeGenre.isEmpty() ? "active" : "" %>" href="<%= ctx %>/home">All</a>
    <% if (genres != null) {
        for (String genre : genres) { %>
    <a class="chip <%= genre.equals(activeGenre) ? "active" : "" %>" href="<%= ctx %>/home?genre=<%= Html.esc(genre) %>"><%= Html.esc(genre) %></a>
    <%  }
    } %>
</div>

<%
    boolean anyNow = false;
    boolean anySoon = false;
    if (movies != null) {
        for (Movie movie : movies) {
            if (movie.isNowShowing()) anyNow = true;
            else anySoon = true;
        }
    }
%>

<% if (movies == null || movies.isEmpty()) { %>
<div class="empty">No films match this search.</div>
<% } %>

<% if (anyNow) { %>
<div class="section-head"><h2>Now showing</h2></div>
<div class="movie-grid">
    <% for (Movie movie : movies) { if (!movie.isNowShowing()) continue; %>
    <a class="movie-card" href="<%= ctx %>/movie?id=<%= movie.getId() %>">
        <div class="poster" style="background: <%= Html.color(movie.getPosterColor()) %>">
            <span class="badge"><%= Html.esc(movie.getCertificate()) %></span>
            <em><%= Html.esc(movie.getTitle().substring(0, 1)) %></em>
        </div>
        <div class="card-body">
            <h3><%= Html.esc(movie.getTitle()) %></h3>
            <p><%= Html.esc(movie.getGenre()) %> · <%= Html.esc(Dates.runtime(movie.getDurationMin())) %></p>
        </div>
    </a>
    <% } %>
</div>
<% } %>

<% if (anySoon) { %>
<div class="section-head"><h2>Coming soon</h2></div>
<div class="movie-grid">
    <% for (Movie movie : movies) { if (movie.isNowShowing()) continue; %>
    <a class="movie-card" href="<%= ctx %>/movie?id=<%= movie.getId() %>">
        <div class="poster" style="background: <%= Html.color(movie.getPosterColor()) %>">
            <span class="badge">Soon</span>
            <em><%= Html.esc(movie.getTitle().substring(0, 1)) %></em>
        </div>
        <div class="card-body">
            <h3><%= Html.esc(movie.getTitle()) %></h3>
            <p><%= Html.esc(movie.getGenre()) %> · <%= Html.esc(movie.getLanguage()) %></p>
        </div>
    </a>
    <% } %>
</div>
<% } %>

<jsp:include page="/includes/shell-end.jsp"/>
