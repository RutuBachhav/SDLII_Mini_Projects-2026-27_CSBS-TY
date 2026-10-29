<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.time.LocalDate, com.entity.Movie, com.entity.Screen, com.entity.Show, com.util.Dates, com.util.Html" %>
<%
    request.setAttribute("pageTitle", "Shows");
    request.setAttribute("playTitle", "Studio");
    request.setAttribute("playSub", "Aurora Grand and Nova Multiplex, 8 rows by 10 seats");
    List<Show> shows = (List<Show>) request.getAttribute("shows");
    List<Movie> movies = (List<Movie>) request.getAttribute("movies");
    List<Screen> screens = (List<Screen>) request.getAttribute("screens");
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/shell-start.jsp"/>
<section class="panel">
    <h2 style="margin-top:0">New show</h2>
    <form method="post" action="<%= ctx %>/admin/shows">
        <input type="hidden" name="action" value="save">
        <div class="form-grid">
            <label class="field"><span>Film</span>
                <select name="movieId" required>
                    <% if (movies != null) {
                        for (Movie movie : movies) { %>
                    <option value="<%= movie.getId() %>"><%= Html.esc(movie.getTitle()) %></option>
                    <%  }
                    } %>
                </select>
            </label>
            <label class="field"><span>Screen</span>
                <select name="screenId" required>
                    <% if (screens != null) {
                        for (Screen screen : screens) { %>
                    <option value="<%= screen.getId() %>"><%= Html.esc(screen.getLabel()) %></option>
                    <%  }
                    } %>
                </select>
            </label>
            <label class="field"><span>Date</span><input type="date" name="showDate" value="<%= LocalDate.now() %>" required></label>
            <label class="field"><span>Time</span><input type="time" name="showTime" value="18:00" required></label>
            <label class="field"><span>Price (₹)</span><input type="number" name="price" min="1" max="10000" step="1" value="250" required></label>
        </div>
        <div class="form-actions">
            <button class="btn btn-green" type="submit">Add show</button>
        </div>
    </form>
</section>

<div class="table-wrap">
    <table>
        <thead>
        <tr><th>Film</th><th>When</th><th>Screen</th><th>Price</th><th>Open seats</th><th></th></tr>
        </thead>
        <tbody>
        <% if (shows != null) {
            for (Show show : shows) { %>
        <tr>
            <td><strong><%= Html.esc(show.getMovieTitle()) %></strong></td>
            <td><%= Html.esc(Dates.formatDate(show.getShowDate())) %><div class="muted"><%= Html.esc(Dates.formatTime(show.getShowTime())) %></div></td>
            <td><%= Html.esc(show.getTheaterName()) %><div class="muted"><%= Html.esc(show.getScreenName()) %></div></td>
            <td><%= Html.esc(Dates.rupees(show.getPrice())) %></td>
            <td><%= show.getSeatsLeft() %></td>
            <td>
                <form method="post" action="<%= ctx %>/admin/shows">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="showId" value="<%= show.getId() %>">
                    <button class="btn btn-danger small" type="submit">Delete</button>
                </form>
            </td>
        </tr>
        <%  }
        } %>
        </tbody>
    </table>
</div>
<jsp:include page="/includes/shell-end.jsp"/>
