<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.entity.Movie, com.util.Dates, com.util.Html" %>
<%
    request.setAttribute("pageTitle", "Films");
    request.setAttribute("playTitle", "Studio");
    request.setAttribute("playSub", "Add a film or update the ones already playing");
    List<Movie> movies = (List<Movie>) request.getAttribute("movies");
    Movie edit = (Movie) request.getAttribute("editMovie");
    if (edit == null) {
        edit = new Movie();
        edit.setCertificate("UA");
        edit.setStatus("NOW_SHOWING");
        edit.setPosterColor("#3d5a80");
        edit.setDurationMin(120);
    }
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/shell-start.jsp"/>
<section class="panel">
    <h2 style="margin-top:0"><%= edit.getId() > 0 ? "Edit film" : "New film" %></h2>
    <form method="post" action="<%= ctx %>/admin/movies">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="movieId" value="<%= edit.getId() %>">
        <div class="form-grid">
            <label class="field"><span>Title</span><input name="title" value="<%= Html.esc(edit.getTitle()) %>" required></label>
            <label class="field"><span>Genre</span><input name="genre" value="<%= Html.esc(edit.getGenre()) %>" required></label>
            <label class="field"><span>Language</span><input name="language" value="<%= Html.esc(edit.getLanguage()) %>" required></label>
            <label class="field"><span>Duration (minutes)</span><input type="number" name="durationMin" min="1" max="400" value="<%= edit.getDurationMin() %>" required></label>
            <label class="field"><span>Certificate</span>
                <select name="certificate">
                    <% for (String cert : new String[]{"U", "UA", "A"}) { %>
                    <option value="<%= cert %>" <%= cert.equals(edit.getCertificate()) ? "selected" : "" %>><%= cert %></option>
                    <% } %>
                </select>
            </label>
            <label class="field"><span>Status</span>
                <select name="status">
                    <option value="NOW_SHOWING" <%= "NOW_SHOWING".equals(edit.getStatus()) ? "selected" : "" %>>Now showing</option>
                    <option value="COMING_SOON" <%= "COMING_SOON".equals(edit.getStatus()) ? "selected" : "" %>>Coming soon</option>
                </select>
            </label>
            <label class="field"><span>Poster colour</span><input type="color" name="posterColor" value="<%= Html.color(edit.getPosterColor()) %>"></label>
            <label class="field wide"><span>Description</span><textarea name="description"><%= Html.esc(edit.getDescription()) %></textarea></label>
        </div>
        <div class="form-actions">
            <button class="btn btn-green" type="submit"><%= edit.getId() > 0 ? "Save" : "Add film" %></button>
            <% if (edit.getId() > 0) { %><a class="btn" href="<%= ctx %>/admin/movies">Cancel</a><% } %>
        </div>
    </form>
</section>

<div class="table-wrap">
    <table>
        <thead>
        <tr><th>Film</th><th>Genre</th><th>Runtime</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
        <% if (movies != null) {
            for (Movie movie : movies) { %>
        <tr>
            <td><strong><%= Html.esc(movie.getTitle()) %></strong><div class="muted"><%= Html.esc(movie.getLanguage()) %> · <%= Html.esc(movie.getCertificate()) %></div></td>
            <td><%= Html.esc(movie.getGenre()) %></td>
            <td><%= Html.esc(Dates.runtime(movie.getDurationMin())) %></td>
            <td><%= movie.isNowShowing() ? "Now showing" : "Coming soon" %></td>
            <td class="actions">
                <a class="btn small" href="<%= ctx %>/admin/movies?edit=<%= movie.getId() %>">Edit</a>
                <form method="post" action="<%= ctx %>/admin/movies">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="movieId" value="<%= movie.getId() %>">
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
