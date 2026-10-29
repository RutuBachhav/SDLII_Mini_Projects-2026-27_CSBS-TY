<%@ page import="com.util.Html" %>
    </main>
<%
    String playTitle = (String) request.getAttribute("playTitle");
    String playSub = (String) request.getAttribute("playSub");
    String playHref = (String) request.getAttribute("playHref");
    String playLabel = (String) request.getAttribute("playLabel");
    String ctx = request.getContextPath();
%>
    <footer class="site-footer">
        <div class="footer-cols">
            <div>
                <h2>Explore</h2>
                <a href="<%= ctx %>/home">Films</a>
                <a href="<%= ctx %>/home">Showtimes</a>
            </div>
            <div>
                <h2>Account</h2>
                <a href="<%= ctx %>/bookings">Tickets</a>
                <a href="<%= ctx %>/login">Log in</a>
                <a href="<%= ctx %>/register">Create account</a>
            </div>
            <div>
                <h2>Cinebook</h2>
                <a href="<%= ctx %>/admin/movies">Studio</a>
                <a href="<%= ctx %>/admin/shows">Shows</a>
                <a href="<%= ctx %>/admin/bookings">Sales</a>
            </div>
        </div>
        <div class="legal-band">
            <span>© 2026 Cinebook</span>
            <span>Pune · Aurora Grand · Nova Multiplex</span>
        </div>
    </footer>
    <% if (playHref != null && playLabel != null) { %>
    <div class="reserve-bar">
        <div>
            <strong><%= Html.esc(playTitle) %></strong>
            <span><%= Html.esc(playSub) %></span>
        </div>
        <a class="btn btn-green" href="<%= Html.esc(playHref) %>"><%= Html.esc(playLabel) %></a>
    </div>
    <% } %>
</div>
</body>
</html>
