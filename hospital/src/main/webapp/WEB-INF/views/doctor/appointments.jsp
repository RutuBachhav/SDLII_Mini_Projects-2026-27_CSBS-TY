<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Appointment"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@ include file="../_head.jsp" %>
<%
    List<Appointment> shown = (List<Appointment>) request.getAttribute("shown");
    String filter = (String) request.getAttribute("filter");
    Integer pendingCount = (Integer) request.getAttribute("pendingCount");
    Integer upcomingCount = (Integer) request.getAttribute("upcomingCount");
    Integer pastCount = (Integer) request.getAttribute("pastCount");
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    String csrf = SessionUtil.csrf(request);
%>
<div class="page-head">
    <h1>Schedule</h1>
    <p>Decide on new requests, run today's list, and close out finished visits with a note.</p>
</div>

<nav aria-label="Schedule filters" class="filter-nav">
    <a class="btn <%= "upcoming".equals(filter) ? "" : "btn-secondary" %> btn-small" href="<%= _ctx %>/doctor/appointments?status=upcoming">Upcoming (<%= upcomingCount %>)</a>
    <a class="btn <%= "pending".equals(filter) ? "" : "btn-secondary" %> btn-small" href="<%= _ctx %>/doctor/appointments?status=pending">Needs decision (<%= pendingCount %>)</a>
    <a class="btn <%= "past".equals(filter) ? "" : "btn-secondary" %> btn-small" href="<%= _ctx %>/doctor/appointments?status=past">Past (<%= pastCount %>)</a>
</nav>

<% if (shown == null || shown.isEmpty()) { %>
    <div class="empty">
        <% if ("pending".equals(filter)) { %>
            <h2>All caught up</h2>
            <p>No requests are waiting for your decision. New bookings will appear here first.</p>
        <% } else if ("past".equals(filter)) { %>
            <h2>No past appointments yet</h2>
            <p>Completed, declined and cancelled visits will be recorded here.</p>
        <% } else { %>
            <h2>Nothing on the schedule</h2>
            <p>No upcoming appointments. Patient requests will appear here once booked.</p>
        <% } %>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Appointments in the selected view</caption>
        <thead>
            <tr><th scope="col">Date</th><th scope="col">Time</th><th scope="col">Patient</th><th scope="col">Phone</th><th scope="col">Reason</th><th scope="col">Status</th><th scope="col">Note</th><th scope="col"><span class="sr-only">Actions</span></th></tr>
        </thead>
        <tbody>
            <% for (Appointment a : shown) { %>
            <tr>
                <td><%= ValidationUtil.esc(a.getDate().format(dayFmt)) %></td>
                <td><%= ValidationUtil.esc(a.getStartTime().toString()) %></td>
                <td><strong><%= ValidationUtil.esc(a.getPatient().getName()) %></strong></td>
                <td><%= ValidationUtil.esc(a.getPatient().getPhone() == null ? "—" : a.getPatient().getPhone()) %></td>
                <td><%= ValidationUtil.esc(a.getReason() == null || a.getReason().isEmpty() ? "—" : a.getReason()) %></td>
                <td><span class="status-word status-word--<%= a.getStatus().name().toLowerCase() %>"><%= ValidationUtil.esc(a.getStatus().name()) %></span></td>
                <td><%= ValidationUtil.esc(a.getNotes() == null || a.getNotes().isEmpty() ? "—" : a.getNotes()) %></td>
                <td class="row-actions">
                    <% if (a.getStatus().name().equals("PENDING")) { %>
                        <form class="inline-form" action="<%= _ctx %>/doctor/appointments/action" method="post">
                            <input type="hidden" name="_csrf" value="<%= csrf %>">
                            <input type="hidden" name="id" value="<%= a.getId() %>">
                            <input type="hidden" name="action" value="accept">
                            <button type="submit" class="btn btn-small">Accept</button>
                        </form>
                        <form class="inline-form" action="<%= _ctx %>/doctor/appointments/action" method="post">
                            <input type="hidden" name="_csrf" value="<%= csrf %>">
                            <input type="hidden" name="id" value="<%= a.getId() %>">
                            <input type="hidden" name="action" value="reject">
                            <button type="submit" class="btn btn-secondary btn-small"
                                data-confirm="The patient will see this visit as declined."
                                data-confirm-title="Decline this request?"
                                data-confirm-ok="Decline visit">Decline</button>
                        </form>
                    <% } else if (a.getStatus().name().equals("CONFIRMED")) { %>
                        <form class="inline-form" action="<%= _ctx %>/doctor/appointments/action" method="post">
                            <input type="hidden" name="_csrf" value="<%= csrf %>">
                            <input type="hidden" name="id" value="<%= a.getId() %>">
                            <input type="hidden" name="notes" value="">
                            <input type="hidden" name="action" value="complete">
                            <button type="submit" class="btn btn-small">Mark complete</button>
                        </form>
                        <form class="inline-form" action="<%= _ctx %>/doctor/appointments/action" method="post">
                            <input type="hidden" name="_csrf" value="<%= csrf %>">
                            <input type="hidden" name="id" value="<%= a.getId() %>">
                            <input type="hidden" name="action" value="cancel">
                            <button type="submit" class="btn btn-danger btn-small"
                                    data-confirm="The patient will see this visit as cancelled."
                                    data-confirm-title="Cancel this appointment?"
                                    data-confirm-ok="Cancel appointment">Cancel</button>
                        </form>
                    <% } else { %>
                        <span class="muted">—</span>
                    <% } %>
                </td>
            </tr>
            <% if (a.getStatus().name().equals("PENDING") || a.getStatus().name().equals("CONFIRMED")) { %>
            <tr>
                <td colspan="8" class="note-cell">
                    <form class="inline-form note-form" action="<%= _ctx %>/doctor/appointments/action" method="post">
                        <input type="hidden" name="_csrf" value="<%= csrf %>">
                        <input type="hidden" name="id" value="<%= a.getId() %>">
                        <input type="hidden" name="action" value="note">
                        <div class="field note-field">
                            <label for="note-<%= a.getId() %>">Visit note for this patient</label>
                            <input id="note-<%= a.getId() %>" name="notes" type="text" maxlength="2000"
                                   value="<%= ValidationUtil.esc(a.getNotes() == null ? "" : a.getNotes()) %>"
                                   placeholder="e.g. Advised rest, review in two weeks…">
                        </div>
                        <button type="submit" class="btn btn-secondary btn-small">Save note</button>
                    </form>
                </td>
            </tr>
            <% } %>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>
<%@ include file="../_foot.jsp" %>
