<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Appointment"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@ include file="../_head.jsp" %>
<%
    List<Appointment> upcoming = (List<Appointment>) request.getAttribute("upcoming");
    List<Appointment> history = (List<Appointment>) request.getAttribute("history");
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    String csrf = SessionUtil.csrf(request);
%>
<div class="page-head">
    <h1>My appointments</h1>
    <p>Track requests, confirmed visits and past care in one ledger.</p>
    <div class="actions"><a class="btn" href="<%= _ctx %>/patient/doctors">Book a new visit</a></div>
</div>

<h2 class="section-title">Upcoming</h2>
<% if (upcoming == null || upcoming.isEmpty()) { %>
    <div class="empty">
        <h2>Nothing scheduled</h2>
        <p>You have no upcoming appointments. Book a visit and it will appear here as Pending until confirmed.</p>
        <p><a class="btn" href="<%= _ctx %>/patient/doctors">Find a doctor</a></p>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Upcoming appointments</caption>
        <thead>
            <tr><th scope="col">Date</th><th scope="col">Time</th><th scope="col">Doctor</th><th scope="col">Department</th><th scope="col">Status</th><th scope="col">Reason</th><th scope="col"><span class="sr-only">Actions</span></th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : upcoming) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getDate().format(dayFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getStartTime().toString()) %>–<%= ValidationUtil.esc(a.getEndTime().toString()) %></td>
                <td><%= ValidationUtil.esc(a.getDoctor().getName()) %></td>
                <td><%= ValidationUtil.esc(a.getDoctor().getDepartment()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
                <td><%= ValidationUtil.esc(a.getReason() == null || a.getReason().isEmpty() ? "—" : a.getReason()) %></td>
                <td class="row-actions">
                    <form class="inline-form" action="<%= _ctx %>/patient/cancel" method="post">
                        <input type="hidden" name="_csrf" value="<%= csrf %>">
                        <input type="hidden" name="id" value="<%= a.getId() %>">
                        <button type="submit" class="btn btn-danger btn-small"
                                data-confirm="Cancelling frees this slot for another patient."
                                data-confirm-title="Cancel this appointment?"
                                data-confirm-ok="Cancel appointment">Cancel</button>
                    </form>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>

<h2 class="section-title">History</h2>
<% if (history == null || history.isEmpty()) { %>
    <div class="empty">
        <h2>No past visits yet</h2>
        <p>Completed, declined and cancelled appointments will be listed here for your records.</p>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Appointment history</caption>
        <thead>
            <tr><th scope="col">Date</th><th scope="col">Time</th><th scope="col">Doctor</th><th scope="col">Status</th><th scope="col">Doctor's note</th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : history) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getDate().format(dayFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getStartTime().toString()) %></td>
                <td><%= ValidationUtil.esc(a.getDoctor().getName()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
                <td><%= ValidationUtil.esc(a.getNotes() == null || a.getNotes().isEmpty() ? "—" : a.getNotes()) %></td>
            </tr>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>
<%@ include file="../_foot.jsp" %>
