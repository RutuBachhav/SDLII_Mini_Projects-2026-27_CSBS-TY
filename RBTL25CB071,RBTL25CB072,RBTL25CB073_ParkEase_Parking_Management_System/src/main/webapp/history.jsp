
<%@ page import="java.sql.*" %>
<%@ page import="com.parkease.util.DBUtil" %>
<%@ include file="includes/header.jspf" %>
<%
String success=request.getParameter("success");
int completed=0; double revenue=0;
try(Connection con=DBUtil.getConnection(); Statement st=con.createStatement(); ResultSet rs=st.executeQuery("SELECT COUNT(*) sessions, COALESCE(SUM(fee),0) revenue FROM parking_sessions WHERE status='Completed'")){
 if(rs.next()){completed=rs.getInt("sessions");revenue=rs.getDouble("revenue");}
}catch(Exception ex){application.log("History stats failed",ex);}
%>
<section class="page-heading"><div><div class="eyebrow">PARKING OPERATIONS / RECORDS</div><h1>Parking <span>history</span></h1><p class="subhead">A simple record of vehicle visits, durations, and parking fees.</p></div><a class="btn btn-outline" href="<%=ctx%>/slots.jsp">&#8592; Back to slots</a></section>
<%if("checkout".equals(success)){%><div class="alert success">&#10003; Vehicle checked out. Fee calculated and slot released.</div><%}%>
<section class="stats-grid history-stats"><article class="stat-card compact-stat"><div class="stat-top"><span class="stat-icon purple">&#9687;</span><span class="stat-tag">COMPLETED</span></div><div class="stat-number"><%=completed%></div><div class="stat-label">Completed sessions</div></article><article class="stat-card compact-stat"><div class="stat-top"><span class="stat-icon green">&#8377;</span><span class="stat-tag">TOTAL FEES</span></div><div class="stat-number"><small class="currency">&#8377;</small><%=String.format("%.2f",revenue)%></div><div class="stat-label">Recorded parking fees</div></article></section>
<section class="panel table-panel"><div class="panel-heading"><div><h3>Recent sessions</h3><p>Newest activity appears first</p></div><span class="pill">ALL RECORDS</span></div>
<div class="table-wrap"><table><thead><tr><th>VEHICLE</th><th>OWNER</th><th>SLOT</th><th>ENTRY TIME</th><th>EXIT TIME</th><th>FEE</th><th>STATUS</th></tr></thead><tbody>
<%
try(Connection con=DBUtil.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT p.vehicle_number,p.owner_name,p.vehicle_type,s.slot_code,p.entry_time,p.exit_time,p.fee,p.status FROM parking_sessions p JOIN parking_slots s ON s.id=p.slot_id ORDER BY p.entry_time DESC LIMIT 100"); ResultSet rs=ps.executeQuery()){
 boolean any=false;
 while(rs.next()){any=true;boolean parked="Parked".equals(rs.getString("status"));Timestamp exit=rs.getTimestamp("exit_time");BigDecimalHolder holder=new BigDecimalHolder(rs.getBigDecimal("fee")); %>
<tr><td><div class="vehicle-cell"><span class="vehicle-mini"><%= "Car".equals(rs.getString("vehicle_type"))?"&#9632;":"&#9678;"%></span><b><%=rs.getString("vehicle_number")%></b></div><small class="table-sub"><%=rs.getString("vehicle_type")%></small></td><td><%=rs.getString("owner_name")%></td><td><span class="slot-code"><%=rs.getString("slot_code")%></span></td><td><%=rs.getTimestamp("entry_time")%></td><td><%=exit==null?"&#8212;":exit.toString()%></td><td><b><%=holder.value==null?"&#8212;":"&#8377;"+holder.value.setScale(2,java.math.RoundingMode.HALF_UP)%></b></td><td><span class="status-badge <%=parked?"status-active":"status-done"%>"><i></i><%=rs.getString("status")%></span></td></tr>
<%} if(!any){%><tr><td colspan="7" class="empty-table">No parking sessions yet. Register a vehicle to start your records.</td></tr><%}
}catch(Exception ex){application.log("History table failed",ex);%><tr><td colspan="7" class="empty-table">Could not load history. Check the database connection.</td></tr><%}%>
</tbody></table></div><div class="table-footer">Showing up to 100 most recent sessions <span>&#9679; Data stored in MySQL</span></div></section>
<%@ include file="includes/footer.jspf" %>
<%! static class BigDecimalHolder { java.math.BigDecimal value; BigDecimalHolder(java.math.BigDecimal v){value=v;} } %>
