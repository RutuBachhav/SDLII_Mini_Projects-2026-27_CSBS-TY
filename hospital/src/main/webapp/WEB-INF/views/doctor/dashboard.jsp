<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Appointment"%>
<%@page import="com.hospital.module.Doctor"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@ include file="../_head.jsp" %>
<%
    Doctor doctor = (Doctor) request.getAttribute("doctor");
    Map<String, Long> counts = (Map<String, Long>) request.getAttribute("counts");
    List<Appointment> today = (List<Appointment>) request.getAttribute("today");
    List<Appointment> upcoming = (List<Appointment>) request.getAttribute("upcoming");
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM");
    String csrf = SessionUtil.csrf(request);
%>
<div class="page-head">
    <h1><%= ValidationUtil.esc(doctor.getName()) %></h1>
    <p><%= ValidationUtil.esc(doctor.getDepartment()) %> · Hours <%= ValidationUtil.esc(doctor.getWorkStart() + "–" + doctor.getWorkEnd()) %><%= doctor.isActive() ? "" : " · Currently not accepting bookings" %></p>
    <div class="actions">
        <a class="btn" href="<%= _ctx %>/doctor/appointments?status=pending">Review requests</a>
        <a class="btn btn-secondary" href="<%= _ctx %>/doctor/profile">Edit profile &amp; hours</a>
    </div>
</div>

<ul class="stat-strip" aria-label="Appointment counts">
    <li><span class="num"><%= counts.get("PENDING") %></span><span class="lbl">Awaiting decision</span></li>
    <li><span class="num"><%= counts.get("CONFIRMED") %></span><span class="lbl">Confirmed</span></li>
    <li><span class="num"><%= counts.get("TODAY") %></span><span class="lbl">On the books today</span></li>
    <li><span class="num"><%= counts.get("COMPLETED") %></span><span class="lbl">Completed</span></li>
</ul>

<h2 class="section-title">Today's list</h2>
<% if (today == null || today.isEmpty()) { %>
    <div class="empty">
        <h2>A clear day</h2>
        <p>No appointments on the books for today. New patient requests will appear here and under Schedule.</p>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Today's appointments</caption>
        <thead>
            <tr><th scope="col">Time</th><th scope="col">Patient</th><th scope="col">Phone</th><th scope="col">Reason</th><th scope="col">Status</th><th scope="col"><span class="sr-only">Actions</span></th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : today) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getStartTime().format(timeFmt)) %></td>
                <td><strong><%= ValidationUtil.esc(a.getPatient().getName()) %></strong></td>
                <td><%= ValidationUtil.esc(a.getPatient().getPhone() == null ? "—" : a.getPatient().getPhone()) %></td>
                <td><%= ValidationUtil.esc(a.getReason() == null || a.getReason().isEmpty() ? "—" : a.getReason()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
                <td class="row-actions"><a class="btn btn-secondary btn-small" href="<%= _ctx %>/doctor/appointments">Open schedule</a></td>
            </tr>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>

<h2 class="section-title">Next up</h2>
<% if (upcoming == null || upcoming.isEmpty()) { %>
    <p class="section-sub">Nothing booked ahead. When patients request times, they will queue here for your decision.</p>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Upcoming appointments</caption>
        <thead>
            <tr><th scope="col">Date</th><th scope="col">Time</th><th scope="col">Patient</th><th scope="col">Status</th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : upcoming) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getDate().format(dayFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getStartTime().format(timeFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getPatient().getName()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
            </tr>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>
<%@ include file="../_foot.jsp" %>
