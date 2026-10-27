
<%@ page import="java.sql.*" %>
<%@ page import="com.parkease.util.DBUtil" %>
<%@ include file="includes/header.jspf" %>
<%
int total=0, occupied=0, available=0, active=0, today=0;
try(Connection con=DBUtil.getConnection(); Statement st=con.createStatement()){
 try(ResultSet rs=st.executeQuery("SELECT COUNT(*) total, SUM(status='Occupied') occupied, SUM(status='Available') available FROM parking_slots")){
  if(rs.next()){total=rs.getInt("total");occupied=rs.getInt("occupied");available=rs.getInt("available");}
 }
 try(ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM parking_sessions WHERE status='Parked'")){if(rs.next())active=rs.getInt(1);}
 try(ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM parking_sessions WHERE DATE(entry_time)=CURDATE()")){if(rs.next())today=rs.getInt(1);}
}catch(Exception ex){application.log("Dashboard query failed",ex);}
double pct=total==0?0:(occupied*100.0/total);
%>
<section class="welcome-row"><div><div class="eyebrow">TUESDAY &#183; YOUR PARKING AT A GLANCE</div><h1>Every space, <span>in its place.</span></h1><p class="subhead">Manage arrivals, track availability, and keep your parking running smoothly.</p></div><a class="btn btn-primary" href="<%=ctx%>/entry.jsp">&#43; Register vehicle</a></section>
<section class="stats-grid">
 <article class="stat-card"><div class="stat-top"><span class="stat-icon purple">&#9636;</span><span class="stat-tag">CAPACITY</span></div><div class="stat-number"><%=total%></div><div class="stat-label">Total parking slots</div><div class="stat-foot">Across cars and bikes</div></article>
 <article class="stat-card"><div class="stat-top"><span class="stat-icon orange">&#9673;</span><span class="stat-tag">IN USE</span></div><div class="stat-number"><%=occupied%><small> / <%=total%></small></div><div class="stat-label">Occupied slots</div><div class="mini-progress"><span style="width:<%=pct%>%"></span></div></article>
 <article class="stat-card"><div class="stat-top"><span class="stat-icon green">&#10003;</span><span class="stat-tag">READY</span></div><div class="stat-number"><%=available%></div><div class="stat-label">Available spaces</div><div class="stat-foot"><span class="green-text">&#9679;</span> Ready for arrivals</div></article>
 <article class="stat-card"><div class="stat-top"><span class="stat-icon blue">&#8599;</span><span class="stat-tag">TODAY</span></div><div class="stat-number"><%=today%></div><div class="stat-label">Vehicle entries today</div><div class="stat-foot">Active sessions: <b><%=active%></b></div></article>
</section>
<section class="dashboard-grid">
 <article class="panel occupancy-panel"><div class="panel-heading"><div><h3>Space overview</h3><p>A quick look at your parking capacity</p></div><span class="pill">LIVE STATUS</span></div>
 <div class="occupancy-content"><div class="ring" style="--progress:<%=pct*3.6%>deg"><div class="ring-inner"><strong><%=Math.round(pct)%>%</strong><span>occupied</span></div></div><div class="legend"><div><i class="legend-dot occupied-dot"></i><span>Occupied</span><b><%=occupied%></b></div><div><i class="legend-dot available-dot"></i><span>Available</span><b><%=available%></b></div><div><i class="legend-dot total-dot"></i><span>Total spaces</span><b><%=total%></b></div></div></div>
 <a class="text-link" href="<%=ctx%>/slots.jsp">Explore all parking slots <span>&#8594;</span></a></article>
 <article class="panel quick-panel"><div class="panel-heading"><div><h3>Quick actions</h3><p>Get things moving in a few clicks</p></div></div>
 <a class="action-card" href="<%=ctx%>/entry.jsp"><span class="action-symbol purple-bg">&#43;</span><span><b>New vehicle entry</b><small>Assign a slot to an arriving vehicle</small></span><span class="arrow">&#8599;</span></a>
 <a class="action-card" href="<%=ctx%>/slots.jsp"><span class="action-symbol green-bg">&#9638;</span><span><b>Manage parking slots</b><small>View availability and release spaces</small></span><span class="arrow">&#8599;</span></a>
 <a class="action-card" href="<%=ctx%>/history.jsp"><span class="action-symbol blue-bg">&#9687;</span><span><b>View parking history</b><small>Review completed sessions and fees</small></span><span class="arrow">&#8599;</span></a>
 </article>
</section>
<section class="bottom-banner"><div class="banner-icon">&#10022;</div><div><b>A little more organised. A lot more efficient.</b><p>ParkEase keeps your daily parking operations clear and connected.</p></div><span class="banner-decoration">P.</span></section>
<%@ include file="includes/footer.jspf" %>
