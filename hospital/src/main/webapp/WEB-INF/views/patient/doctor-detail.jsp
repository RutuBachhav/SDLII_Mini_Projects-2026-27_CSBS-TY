<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Doctor"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.time.LocalTime"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@ include file="../_head.jsp" %>
<%
    Doctor doctor = (Doctor) request.getAttribute("doctor");
    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    List<LocalDate> days = (List<LocalDate>) request.getAttribute("days");
    List<LocalTime> slots = (List<LocalTime>) request.getAttribute("slots");
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM");
    DateTimeFormatter fullFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    String csrf = SessionUtil.csrf(request);
%>
<p class="mt-0"><a href="<%= _ctx %>/patient/doctors">&larr; Back to all doctors</a></p>
<div class="page-head">
    <h1><%= ValidationUtil.esc(doctor.getName()) %></h1>
    <p><%= ValidationUtil.esc(doctor.getDepartment()) %><%= doctor.getQualification() != null ? " · " + ValidationUtil.esc(doctor.getQualification()) : "" %><%= doctor.getConsultationFee() != null ? " · ₹" + doctor.getConsultationFee() + " per visit" : "" %></p>
</div>

<div class="columns two">
    <div>
        <h2 class="section-title mt-0">About</h2>
        <dl class="record">
            <dt>Department</dt><dd><%= ValidationUtil.esc(doctor.getDepartment()) %></dd>
            <dt>Qualification</dt><dd><%= ValidationUtil.esc(doctor.getQualification() == null ? "Not specified" : doctor.getQualification()) %></dd>
            <dt>Working hours</dt><dd><%= ValidationUtil.esc(doctor.getWorkStart() + " – " + doctor.getWorkEnd()) %>, <%= doctor.getSlotMinutes() %>-minute slots</dd>
            <dt>Consultation fee</dt><dd><%= doctor.getConsultationFee() == null ? "Not specified" : "₹" + doctor.getConsultationFee() %></dd>
        </dl>
        <% if (doctor.getBio() != null && !doctor.getBio().trim().isEmpty()) { %>
            <p><%= ValidationUtil.esc(doctor.getBio()) %></p>
        <% } %>
    </div>
    <div>
        <h2 class="section-title mt-0">Book a time</h2>
        <form action="<%= _ctx %>/patient/doctor" method="get" class="form">
            <input type="hidden" name="id" value="<%= doctor.getId() %>">
            <div class="field">
                <label for="date">Choose a day</label>
                <select id="date" name="date" onchange="this.form.submit()">
                    <% for (LocalDate d : days) { %>
                        <option value="<%= d.toString() %>" <%= d.equals(selectedDate) ? "selected" : "" %>><%= ValidationUtil.esc(d.format(dayFmt)) %></option>
                    <% } %>
                </select>
                <p class="hint" id="date-hint">Changing the day reloads the open times below.</p>
            </div>
            <button class="btn btn-secondary" type="submit">Show times</button>
        </form>
        <p class="section-sub">Available times for <strong><%= ValidationUtil.esc(selectedDate.format(fullFmt)) %></strong>. Your request starts as Pending; the doctor confirms it.</p>
        <% if (slots == null || slots.isEmpty()) { %>
            <div class="empty">
                <h2>No open times this day</h2>
                <p>Every slot on this date is taken or has passed. Try another day.</p>
            </div>
        <% } else { %>
            <form action="<%= _ctx %>/patient/book" method="post" class="form">
                <input type="hidden" name="_csrf" value="<%= csrf %>">
                <input type="hidden" name="doctorId" value="<%= doctor.getId() %>">
                <input type="hidden" name="date" value="<%= selectedDate.toString() %>">
                <fieldset class="slot-group">
                    <legend>Available times</legend>
                    <div class="slot-grid">
                        <% int i = 0; for (LocalTime t : slots) { i++; %>
                            <label>
                                <input type="radio" name="time" value="<%= t.toString() %>" <%= i == 1 ? "checked" : "" %>>
                                <span><%= ValidationUtil.esc(t.toString()) %></span>
                            </label>
                        <% } %>
                    </div>
                </fieldset>
                <div class="field">
                    <label for="reason">Reason for visit <span class="opt">(optional)</span></label>
                    <textarea id="reason" name="reason" maxlength="500" placeholder="Briefly describe your symptoms or the purpose of the visit…"></textarea>
                </div>
                <button class="btn" type="submit">Request appointment</button>
            </form>
        <% } %>
    </div>
</div>
<%@ include file="../_foot.jsp" %>
