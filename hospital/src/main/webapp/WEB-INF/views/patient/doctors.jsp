<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Doctor"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@ include file="../_head.jsp" %>
<%
    List<Doctor> doctors = (List<Doctor>) request.getAttribute("doctors");
    Set<String> specialties = (Set<String>) request.getAttribute("specialties");
    String specialty = (String) request.getAttribute("specialty");
    String q = (String) request.getAttribute("q");
%>
<div class="page-head">
    <h1>Find a doctor</h1>
    <p>Browse by department or search by name. Open a profile to see working hours and book a time.</p>
</div>

<form class="form wide" action="<%= _ctx %>/patient/doctors" method="get" role="search" aria-label="Find a doctor">
    <div class="grid-pair">
        <div class="field">
            <label for="q">Search by name</label>
            <input id="q" name="q" type="text" value="<%= ValidationUtil.esc(q) %>" placeholder="e.g. Rao…" autocomplete="off">
        </div>
        <div class="field">
            <label for="specialty">Department</label>
            <select id="specialty" name="specialty">
                <option value="">All departments</option>
                <% for (String s : specialties) { %>
                    <option value="<%= ValidationUtil.esc(s) %>" <%= s.equals(specialty) ? "selected" : "" %>><%= ValidationUtil.esc(s) %></option>
                <% } %>
            </select>
        </div>
    </div>
    <div class="action-row">
        <button class="btn" type="submit">Search</button>
        <% if ((specialty != null && !specialty.isEmpty()) || (q != null && !q.isEmpty())) { %>
            <a class="btn btn-secondary" href="<%= _ctx %>/patient/doctors">Clear</a>
        <% } %>
    </div>
</form>

<h2 class="section-title"><%= doctors.size() %> doctor<%= doctors.size() == 1 ? "" : "s" %> available</h2>
<% if (doctors.isEmpty()) { %>
    <div class="empty">
        <% if ((q != null && !q.isEmpty()) || (specialty != null && !specialty.isEmpty())) { %>
            <h2>No doctors match your search</h2>
            <p>No doctor matches “<%= ValidationUtil.esc(q) %>”<%= specialty.isEmpty() ? "" : " in " + ValidationUtil.esc(specialty) %>. Try a different name or clear the filters.</p>
            <p><a class="btn btn-secondary" href="<%= _ctx %>/patient/doctors">Clear search</a></p>
        <% } else { %>
            <h2>No doctors available right now</h2>
            <p>There are currently no doctors accepting bookings. Please check back later.</p>
        <% } %>
    </div>
<% } else { %>
    <div class="table-scroll">
    <table class="ledger">
        <caption class="sr-only">Doctors matching the current search</caption>
        <thead>
            <tr><th scope="col">Doctor</th><th scope="col">Department</th><th scope="col">Qualification</th><th scope="col">Fee</th><th scope="col">Hours</th><th scope="col"><span class="sr-only">Actions</span></th></tr>
        </thead>
        <tbody>
            <% for (Doctor d : doctors) { %>
            <tr>
                <td><strong><%= ValidationUtil.esc(d.getName()) %></strong></td>
                <td><%= ValidationUtil.esc(d.getDepartment()) %></td>
                <td><%= ValidationUtil.esc(d.getQualification() == null ? "—" : d.getQualification()) %></td>
                <td><%= d.getConsultationFee() == null ? "—" : "₹" + d.getConsultationFee() %></td>
                <td><%= ValidationUtil.esc(d.getWorkStart() + "–" + d.getWorkEnd()) %></td>
                <td class="row-actions"><a class="btn btn-secondary btn-small" href="<%= _ctx %>/patient/doctor?id=<%= d.getId() %>">View &amp; book</a></td>
            </tr>
            <% } %>
        </tbody>
    </table>
    </div>
<% } %>
<%@ include file="../_foot.jsp" %>
