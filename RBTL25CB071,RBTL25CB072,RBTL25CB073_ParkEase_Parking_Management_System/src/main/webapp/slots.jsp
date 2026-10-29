
<%@ page import="java.sql.*" %>
<%@ page import="com.parkease.util.DBUtil" %>
<%@ include file="includes/header.jspf" %>
<%
String success=request.getParameter("success"), error=request.getParameter("error");
int total=0, free=0, busy=0;
%>
<section class="page-heading"><div><div class="eyebrow">PARKING OPERATIONS / FLOOR PLAN</div><h1>Parking <span>slots</span></h1><p class="subhead">See every space at a glance and release slots when vehicles leave.</p></div><a class="btn btn-primary" href="<%=ctx%>/entry.jsp">&#43; New entry</a></section>
<% if("checkin".equals(success)){%><div class="alert success">&#10003; Vehicle entry saved successfully.</div><%}%>
<% if(error!=null){%><div class="alert error">This parking session could not be found. Refresh and try again.</div><%}%>
<div class="slot-summary"><div><span class="legend-dot available-dot"></span> Available <b id="availableCount">&#8212;</b></div><div><span class="legend-dot occupied-dot"></span> Occupied <b id="occupiedCount">&#8212;</b></div><div class="slot-summary-note">Select a vehicle card to check it out.</div></div>
<section class="panel slot-floor"><div class="panel-heading"><div><h3>Live parking map</h3><p>Slot status is updated from the database</p></div><span class="pill"><span class="live-dot"></span> LIVE</span></div>
<div class="floor-section"><div class="floor-label"><span>CAR ZONE</span><span>4-WHEELER</span></div><div class="slot-grid">
<%
try(Connection con=DBUtil.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT s.id,s.slot_code,s.status,p.id session_id,p.vehicle_number,p.owner_name,p.entry_time FROM parking_slots s LEFT JOIN parking_sessions p ON p.slot_id=s.id AND p.status='Parked' WHERE s.slot_type='Car' ORDER BY s.slot_code"); ResultSet rs=ps.executeQuery()){
 while(rs.next()){boolean occ="Occupied".equals(rs.getString("status")); total++; if(occ)busy++;else free++; %>
<div class="parking-slot <%=occ?"is-occupied":"is-free"%>"><div class="slot-top"><span class="slot-indicator"></span><b><%=rs.getString("slot_code")%></b><span class="slot-car"><%=occ?"&#9632;":"&#9671;"%></span></div>
<%if(occ){%><div class="slot-vehicle"><b><%=rs.getString("vehicle_number")%></b><small><%=rs.getString("owner_name")%></small></div><form action="<%=ctx%>/parking" method="post" onsubmit="return confirm('Check out <%=rs.getString("vehicle_number")%> and calculate the parking fee?')"><input type="hidden" name="action" value="checkout"><input type="hidden" name="sessionId" value="<%=rs.getLong("session_id")%>"><button class="slot-button" type="submit">Check out &#8599;</button></form><%}else{%><div class="slot-available-text">Ready to park</div><a class="slot-button" href="<%=ctx%>/entry.jsp">Assign slot &#8599;</a><%}%></div>
<% }}catch(Exception ex){application.log("Car slots failed",ex);%><p class="empty-note">Could not load car slots. Check the database connection.</p><%}%>
</div></div>
<div class="floor-divider"><span>DRIVEWAY</span><div class="driveway-lines"></div><span>&#8595; ENTRY / EXIT &#8595;</span></div>
<div class="floor-section"><div class="floor-label"><span>BIKE ZONE</span><span>2-WHEELER</span></div><div class="slot-grid">
<%
try(Connection con=DBUtil.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT s.id,s.slot_code,s.status,p.id session_id,p.vehicle_number,p.owner_name FROM parking_slots s LEFT JOIN parking_sessions p ON p.slot_id=s.id AND p.status='Parked' WHERE s.slot_type='Bike' ORDER BY s.slot_code"); ResultSet rs=ps.executeQuery()){
 while(rs.next()){boolean occ="Occupied".equals(rs.getString("status")); total++; if(occ)busy++;else free++; %>
<div class="parking-slot bike-slot <%=occ?"is-occupied":"is-free"%>"><div class="slot-top"><span class="slot-indicator"></span><b><%=rs.getString("slot_code")%></b><span class="slot-car"><%=occ?"&#9678;":"&#9671;"%></span></div>
<%if(occ){%><div class="slot-vehicle"><b><%=rs.getString("vehicle_number")%></b><small><%=rs.getString("owner_name")%></small></div><form action="<%=ctx%>/parking" method="post" onsubmit="return confirm('Check out <%=rs.getString("vehicle_number")%> and calculate the parking fee?')"><input type="hidden" name="action" value="checkout"><input type="hidden" name="sessionId" value="<%=rs.getLong("session_id")%>"><button class="slot-button" type="submit">Check out &#8599;</button></form><%}else{%><div class="slot-available-text">Ready to park</div><a class="slot-button" href="<%=ctx%>/entry.jsp">Assign slot &#8599;</a><%}%></div>
<% }}catch(Exception ex){application.log("Bike slots failed",ex);%><p class="empty-note">Could not load bike slots. Check the database connection.</p><%}%>
</div></div></section>
<script>
document.getElementById('availableCount').textContent='<%=free%>';
document.getElementById('occupiedCount').textContent='<%=busy%>';
</script>
<%@ include file="includes/footer.jspf" %>
