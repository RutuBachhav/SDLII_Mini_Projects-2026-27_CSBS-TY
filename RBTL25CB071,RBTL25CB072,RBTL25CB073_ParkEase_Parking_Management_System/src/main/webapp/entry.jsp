
<%@ page import="java.sql.*" %>
<%@ page import="com.parkease.util.DBUtil" %>
<%@ include file="includes/header.jspf" %>
<%
String error=request.getParameter("error");
%>
<section class="page-heading"><div><div class="eyebrow">VEHICLE OPERATIONS / CHECK-IN</div><h1>Vehicle <span>entry</span></h1><p class="subhead">Register an arriving vehicle and assign its parking space.</p></div><a class="btn btn-outline" href="<%=ctx%>/slots.jsp">View slots &#8599;</a></section>
<% if(error!=null){ %><div class="alert error"><b>Could not register vehicle.</b> <%= "duplicate".equals(error)?"This vehicle is already parked.": "slot".equals(error)?"That slot is no longer available or does not match the vehicle type.":"Please check the form and database connection." %></div><% } %>
<div class="form-layout"><section class="panel form-panel"><div class="panel-heading"><div><h3>Vehicle details</h3><p>Fields marked * are required</p></div><span class="step-badge">STEP 01</span></div>
<form action="<%=ctx%>/parking" method="post" class="parking-form">
<input type="hidden" name="action" value="checkin">
<label for="vehicleNumber">Vehicle registration number <span>*</span></label><input id="vehicleNumber" name="vehicleNumber" placeholder="e.g. MH 12 AB 1234" maxlength="20" required>
<div class="form-two"><div><label for="ownerName">Owner / driver name <span>*</span></label><input id="ownerName" name="ownerName" placeholder="Full name" maxlength="100" required></div><div><label for="vehicleType">Vehicle type <span>*</span></label><select id="vehicleType" name="vehicleType" required><option value="Car">Car</option><option value="Bike">Bike</option></select></div></div>
<label>Choose an available slot <span>*</span></label><div class="slot-choice" id="slotChoices">
<%
try(Connection con=DBUtil.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT id,slot_code,slot_type FROM parking_slots WHERE status='Available' ORDER BY slot_type,slot_code"); ResultSet rs=ps.executeQuery()){
 boolean found=false;
 while(rs.next()){found=true; String type=rs.getString("slot_type"); %>
<label class="slot-option" data-type="<%=type%>"><input type="radio" name="slotId" value="<%=rs.getInt("id")%>" required><span class="slot-option-box"><b><%=rs.getString("slot_code")%></b><small><%=type%> space</small></span></label>
<% } if(!found){ %><p class="empty-note">No slots are available right now.</p><% }
}catch(Exception ex){application.log("Slot list failed",ex);%><p class="empty-note">Could not load slots. Check your MySQL connection.</p><%}%>
</div>
<div class="form-actions"><a class="btn btn-outline" href="<%=ctx%>/index.jsp">Cancel</a><button class="btn btn-primary" type="submit">Confirm entry <span>&#8594;</span></button></div>
</form></section>
<aside class="panel tip-panel"><div class="tip-illustration"><div class="sun"></div><div class="road"><span>&#8599;</span><div class="car-shape">&#9632;</div></div></div><span class="pill">QUICK TIP</span><h3>Keep traffic flowing.</h3><p>Double-check the registration number before confirming. A vehicle can only have one active parking session at a time.</p><div class="tip-divider"></div><div class="tip-line"><span>01</span><p>Enter vehicle and owner details</p></div><div class="tip-line"><span>02</span><p>Pick a matching available slot</p></div><div class="tip-line"><span>03</span><p>Confirm entry to save the session</p></div></aside></div>
<script>
const typeSelect=document.getElementById('vehicleType');
function filterSlots(){document.querySelectorAll('.slot-option').forEach(el=>{el.style.display=el.dataset.type===typeSelect.value?'':'none';const input=el.querySelector('input');if(el.dataset.type!==typeSelect.value)input.checked=false;});}
typeSelect.addEventListener('change',filterSlots);filterSlots();
</script>
<%@ include file="includes/footer.jspf" %>
