<%@ page import="com.entity.User, com.util.Auth, com.util.Html" %>
<%
    User me = Auth.current(request);
    String here = request.getServletPath();
    String ctx = request.getContextPath();
    String query = request.getParameter("q");
    String initial = "C";
    if (me != null && me.getName() != null && !me.getName().isBlank()) {
        initial = me.getName().substring(0, 1).toUpperCase();
    }
    boolean films = "/home".equals(here) || "/movie".equals(here) || "/seats".equals(here);
    boolean tickets = "/bookings".equals(here) || "/ticket".equals(here);
%>
<header class="site-header">
    <div class="top-nav">
        <a class="wordmark" href="<%= ctx %>/home">cinebook</a>
        <nav class="product-tabs" aria-label="Primary">
            <a class="<%= films ? "active" : "" %>" href="<%= ctx %>/home">Films</a>
            <a class="<%= tickets ? "active" : "" %>" href="<%= ctx %>/bookings">Tickets</a>
            <% if (me != null && me.isAdmin()) { %>
            <a class="<%= here.startsWith("/admin/movies") ? "active" : "" %>" href="<%= ctx %>/admin/movies">Studio</a>
            <a class="<%= here.startsWith("/admin/shows") ? "active" : "" %>" href="<%= ctx %>/admin/shows">Shows</a>
            <a class="<%= here.startsWith("/admin/bookings") ? "active" : "" %>" href="<%= ctx %>/admin/bookings">Sales</a>
            <% } %>
        </nav>
        <div class="account">
            <% if (me != null) { %>
            <span class="avatar"><%= Html.esc(initial) %></span>
            <span class="account-name"><%= Html.esc(me.getName()) %></span>
            <a class="text-link" href="<%= ctx %>/logout">Log out</a>
            <% } else { %>
            <a class="text-link" href="<%= ctx %>/login">Log in</a>
            <a class="btn btn-pill" href="<%= ctx %>/register">Sign up</a>
            <% } %>
        </div>
    </div>
    <form class="search-form" action="<%= ctx %>/home" method="get">
        <label class="search-segment">
            <span>Films</span>
            <input class="search" type="search" name="q" value="<%= Html.esc(query) %>" placeholder="Search titles, genres, languages" aria-label="Search films">
        </label>
        <button class="search-orb" type="submit" aria-label="Search">
            <svg viewBox="0 0 32 32" width="16" height="16" aria-hidden="true">
                <path fill="none" stroke="#fff" stroke-width="3" d="M13 24a11 11 0 1 0 0-22 11 11 0 0 0 0 22zm8-3 8 8"/>
            </svg>
        </button>
    </form>
</header>
