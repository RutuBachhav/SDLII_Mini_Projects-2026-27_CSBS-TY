<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Appointment"%>
<%@page import="com.hospital.module.Patient"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@ include file="../_head.jsp" %>
<%
    Patient patient = (Patient) request.getAttribute("patient");
    List<Appointment> upcoming = (List<Appointment>) request.getAttribute("upcoming");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    String csrf = SessionUtil.csrf(request);
%>
<%
    int _hour = java.time.LocalTime.now().getHour();
    String _greet = _hour < 12 ? "Good morning" : (_hour < 17 ? "Good afternoon" : "Good evening");
%>
<div class="page-head">
    <h1><%= _greet %>, <%= ValidationUtil.esc(patient.getName()) %></h1>
    <p>Your upcoming visits and the fastest way to book the next one.</p>
    <div class="actions">
        <a class="btn" href="<%= _ctx %>/patient/doctors">Find a doctor</a>
        <a class="btn btn-secondary" href="<%= _ctx %>/patient/appointments">All appointments</a>
    </div>
</div>

<h2 class="section-title">Upcoming visits</h2>
<% if (upcoming == null || upcoming.isEmpty()) { %>
    <div class="empty">
        <h2>No visits scheduled</h2>
        <p>When you book an appointment it will appear here, with its current status. Start by choosing a doctor.</p>
        <p><a class="btn" href="<%= _ctx %>/patient/doctors">Find a doctor</a></p>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Upcoming visits</caption>
        <thead>
            <tr><th scope="col">Date</th><th scope="col">Time</th><th scope="col">Doctor</th><th scope="col">Department</th><th scope="col">Status</th><th scope="col"><span class="sr-only">Actions</span></th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : upcoming) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getDate().format(dayFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getStartTime().toString()) %></td>
                <td><%= ValidationUtil.esc(a.getDoctor().getName()) %></td>
                <td><%= ValidationUtil.esc(a.getDoctor().getDepartment()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
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
    <p class="section-sub">Showing <%= upcoming.size() %> of <%= totalCount %> appointments in total.</p>
<% } %>
<%@ include file="../_foot.jsp" %>
