<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.entity.Room"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Book Room - Seaside Grand Hotel</title>
<link rel="icon" href="data:,">
<link rel="stylesheet" href="css/hotel.css">
</head>
<body>
<header class="topbar">
  <a class="brand" href="Home.html">Seaside Grand <span>Hotel</span></a>
  <nav aria-label="Main">
    <a href="Home.html">Home</a>
    <a href="AvailableRoomsServlet">Rooms</a>
  </nav>
</header>

<main class="page">
  <div class="page-head">
    <p class="kicker">Almost yours</p>
    <h1>Confirm your booking</h1>
    <p>Review the room, add your details &mdash; done in under a minute.</p>
  </div>

<%
    Room room = (Room) request.getAttribute("room");
%>
  <div class="narrow">
    <div class="form-card">
      <div class="summary" aria-label="Selected room" style="text-align:center">
        <b>Room <%= room.getRoomNumber() %></b> &middot; <%= room.getRoomType() %><br>
        Rs. <%= room.getPriceDisplay() %> per night &middot; breakfast included<br>
        <span style="font-size:13px;color:var(--muted)">Free cancellation until 24 hrs before check-in</span>
      </div>

      <form action="BookingServlet" method="post" id="bookForm" novalidate>
        <input type="hidden" name="roomId" value="<%= room.getId() %>">

        <p class="group-title">Guest details</p>
        <div class="field">
          <label for="customerName">Full name</label>
          <input type="text" id="customerName" name="customerName"
                 placeholder="e.g. Atharv Sharma" required autocomplete="name">
        </div>
        <div class="field">
          <label for="email">Email</label>
          <input type="email" id="email" name="email"
                 placeholder="you@example.com" required autocomplete="email">
          <p class="hint">We send your confirmation here.</p>
        </div>
        <div class="field">
          <label for="phone">Phone</label>
          <input type="tel" id="phone" name="phone"
                 placeholder="e.g. 9876543210" required autocomplete="tel">
        </div>

        <p class="group-title">Your stay</p>
        <div class="field">
          <label for="checkInDate">Check-in date</label>
          <input type="date" id="checkInDate" name="checkInDate" required>
        </div>
        <div class="field">
          <label for="checkOutDate">Check-out date</label>
          <input type="date" id="checkOutDate" name="checkOutDate" required>
          <p class="hint" id="dateHint">Check-out must be after check-in.</p>
        </div>
        <div class="field">
          <label for="numberOfGuests">Number of guests</label>
          <input type="number" id="numberOfGuests" name="numberOfGuests"
                 min="1" max="10" value="2" required>
        </div>

        <button class="btn btn-primary btn-block" type="submit">Confirm Booking</button>
      </form>
      <a class="btn btn-quiet btn-block" href="AvailableRoomsServlet">Back to Rooms</a>
    </div>
  </div>

  <div class="ticks" aria-label="Booking promises">
    <div class="tick"><span class="mark">&#10003;</span><b>Instant confirmation</b>No waiting &mdash; your room is held at once.</div>
    <div class="tick"><span class="mark">&#10003;</span><b>Best price promise</b>The rate you see is the rate you pay.</div>
    <div class="tick"><span class="mark">&#10003;</span><b>Free cancellation</b>Full refund until 24 hrs before check-in.</div>
    <div class="tick"><span class="mark">&#10003;</span><b>24-hr front desk</b>Humans, around the clock, every day.</div>
  </div>
</main>

<footer class="footer">&copy; 2026 Seaside Grand Hotel</footer>

<script>
// Error prevention: no past dates, checkout always after checkin.
// Server still validates everything - this is just a head start.
(function(){
  var inEl = document.getElementById('checkInDate');
  var outEl = document.getElementById('checkOutDate');
  var today = new Date().toISOString().split('T')[0];
  inEl.min = today;
  outEl.min = today;
  inEl.addEventListener('change', function(){
    outEl.min = inEl.value || today;
    if (outEl.value && outEl.value <= inEl.value) outEl.value = '';
  });
  document.getElementById('bookForm').addEventListener('submit', function(e){
    if (outEl.value <= inEl.value) {
      e.preventDefault();
      document.getElementById('dateHint').style.color = '#b3261e';
      outEl.focus();
    }
  });
})();
</script>
</body>
</html>
