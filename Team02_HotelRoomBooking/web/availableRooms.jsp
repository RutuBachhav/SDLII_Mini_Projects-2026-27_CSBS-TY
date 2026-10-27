<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,com.entity.Room"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Available Rooms - Seaside Grand Hotel</title>
<link rel="icon" href="data:,">
<link rel="stylesheet" href="css/hotel.css">
</head>
<body>
<header class="topbar">
  <a class="brand" href="Home.html">Seaside Grand <span>Hotel</span></a>
  <nav aria-label="Main">
    <a href="Home.html">Home</a>
    <a href="addRoom.html">Add Room</a>
  </nav>
</header>

<main class="page">
  <div class="page-head">
    <p class="kicker">Pick your stay</p>
    <h1>Available rooms</h1>
    <p>Every room below is open tonight. Choose one to start your booking.</p>
  </div>

<%
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    if (rooms == null || rooms.isEmpty()) {
%>
  <div class="empty">
    <p class="big">All booked out.</p>
    <p>No rooms are available right now &mdash; check back soon, or add a new room below.</p>
    <a class="btn btn-primary" href="addRoom.html">Add a Room</a><br>
    <a class="btn btn-quiet" href="Home.html">Back to Home</a>
  </div>
<%
    } else {
%>
  <% Room first = rooms.get(0); %>
  <article class="room-featured">
    <div class="room-no" aria-hidden="true"><%= first.getRoomNumber() %></div>
    <div class="room-meta">
      <p class="kicker">&#10022; Tonight&rsquo;s pick &middot; <%= first.getRoomType() %></p>
      <h3>Room <%= first.getRoomNumber() %></h3>
      <p>Sea-facing &middot; breakfast included &middot; free Wi-Fi &middot; best availability tonight</p>
    </div>
    <div class="room-book">
      <p class="price">Rs. <%= first.getPriceDisplay() %><small>per night</small></p>
      <a class="btn btn-primary" href="BookingServlet?roomId=<%= first.getId() %>">Book Now</a>
    </div>
  </article>
  <% if (rooms.size() > 1) { %>
  <div class="rooms">
    <% int i = 0; for (int k = 1; k < rooms.size(); k++) { Room r = rooms.get(k); i++; %>
    <article class="room-card stagger-<%= (i % 6) + 1 %>">
      <div class="room-no" aria-hidden="true"><%= r.getRoomNumber() %></div>
      <div class="room-meta">
        <p class="kicker"><%= r.getRoomType() %></p>
        <h3>Room <%= r.getRoomNumber() %></h3>
        <p>Sea-facing &middot; breakfast included &middot; free Wi-Fi</p>
      </div>
      <div class="room-book">
        <p class="price">Rs. <%= r.getPriceDisplay() %><small>per night</small></p>
        <a class="btn btn-primary" href="BookingServlet?roomId=<%= r.getId() %>">Book Now</a>
      </div>
    </article>
    <% } %>
  </div>
  <% } %>
  <div class="center-row"><a class="btn btn-quiet" href="Home.html">Back to Home</a></div>
<%
    }
%>
</main>

<footer class="footer">&copy; 2026 Seaside Grand Hotel</footer>
</body>
</html>
