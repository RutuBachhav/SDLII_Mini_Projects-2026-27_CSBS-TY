<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.Doctor"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@ include file="../_head.jsp" %>
<%
    Doctor doctor = (Doctor) request.getAttribute("doctor");
    String csrf = SessionUtil.csrf(request);
    String phone = pick(request, "phone", str(doctor.getPhone()));
    String qualification = pick(request, "qualification", str(doctor.getQualification()));
    String bio = pick(request, "bio", str(doctor.getBio()));
    String feeRaw = pick(request, "feeRaw", doctor.getConsultationFee() == null ? "" : String.valueOf(doctor.getConsultationFee()));
    String workStart = pick(request, "workStart", str(doctor.getWorkStart()));
    String workEnd = pick(request, "workEnd", str(doctor.getWorkEnd()));
    String slotRaw = pick(request, "slotRaw", doctor.getSlotMinutes() == null ? "30" : String.valueOf(doctor.getSlotMinutes()));
    Object activeObj = request.getAttribute("activeChecked");
    boolean activeChecked = activeObj != null ? (Boolean) activeObj : doctor.isActive();
%>
<%! private static String pick(javax.servlet.http.HttpServletRequest r, String k, String fallback) {
        Object v = r.getAttribute(k);
        if (v != null) { return String.valueOf(v); }
        return fallback == null ? "" : fallback;
    }
    private static String str(String v) { return v == null ? "" : v; }
%>
<div class="page-head">
    <h1>My profile &amp; availability</h1>
    <p>What patients see when they book with you, and the hours you accept appointments.</p>
</div>
<form class="form wide" action="<%= _ctx %>/doctor/profile" method="post" novalidate>
    <input type="hidden" name="_csrf" value="<%= csrf %>">
    <div class="field">
        <label for="phone">Contact phone</label>
        <input id="phone" name="phone" type="tel" inputmode="tel" autocomplete="tel"
               value="<%= ValidationUtil.esc(phone) %>" aria-describedby="phone-err"
               <%= request.getAttribute("phoneError") != null ? "aria-invalid=\"true\"" : "" %>>
        <% if (request.getAttribute("phoneError") != null) { %><p class="error" id="phone-err"><%= ValidationUtil.esc((String) request.getAttribute("phoneError")) %></p><% } %>
    </div>
    <div class="field">
        <label for="qualification">Qualification</label>
        <input id="qualification" name="qualification" type="text" maxlength="255"
               value="<%= ValidationUtil.esc(qualification) %>" placeholder="e.g. MD Cardiology…">
    </div>
    <div class="field">
        <label for="bio">Professional summary</label>
        <textarea id="bio" name="bio" maxlength="2000" placeholder="A few lines patients see on your profile…"><%= ValidationUtil.esc(bio) %></textarea>
    </div>
    <div class="field">
        <label for="consultationFee">Consultation fee (₹, optional)</label>
        <input id="consultationFee" name="consultationFee" type="number" min="0" max="100000" step="1"
               value="<%= ValidationUtil.esc(feeRaw) %>" aria-describedby="fee-err"
               <%= request.getAttribute("feeError") != null ? "aria-invalid=\"true\"" : "" %>>
        <% if (request.getAttribute("feeError") != null) { %><p class="error" id="fee-err"><%= ValidationUtil.esc((String) request.getAttribute("feeError")) %></p><% } %>
    </div>
    <h2 class="section-title">Availability</h2>
    <div class="grid-pair">
        <div class="field">
            <label for="workStart">Day starts</label>
            <input id="workStart" name="workStart" type="time" value="<%= ValidationUtil.esc(workStart) %>" required
                   aria-describedby="hours-err" <%= request.getAttribute("hoursError") != null ? "aria-invalid=\"true\"" : "" %>>
        </div>
        <div class="field">
            <label for="workEnd">Day ends</label>
            <input id="workEnd" name="workEnd" type="time" value="<%= ValidationUtil.esc(workEnd) %>" required
                   aria-describedby="hours-err" <%= request.getAttribute("hoursError") != null ? "aria-invalid=\"true\"" : "" %>>
        </div>
    </div>
    <% if (request.getAttribute("hoursError") != null) { %><p class="error" id="hours-err"><%= ValidationUtil.esc((String) request.getAttribute("hoursError")) %></p><% } %>
    <div class="field">
        <label for="slotMinutes">Appointment length</label>
        <select id="slotMinutes" name="slotMinutes" aria-describedby="slot-err"
                <%= request.getAttribute("slotError") != null ? "aria-invalid=\"true\"" : "" %>>
            <% String[] slots = {"15", "20", "30", "45", "60"}; %>
            <% for (String s : slots) { %>
                <option value="<%= s %>" <%= s.equals(slotRaw) ? "selected" : "" %>><%= s %> minutes</option>
            <% } %>
        </select>
        <% if (request.getAttribute("slotError") != null) { %><p class="error" id="slot-err"><%= ValidationUtil.esc((String) request.getAttribute("slotError")) %></p><% } %>
    </div>
    <div class="field">
        <div class="checkbox-row">
            <input id="active" name="active" type="checkbox" value="on" <%= activeChecked ? "checked" : "" %>>
            <label for="active" class="opt">Accepting new bookings <span>— uncheck to pause and hide your profile from patients.</span></label>
        </div>
    </div>
    <button class="btn" type="submit">Save changes</button>
</form>
<%@ include file="../_foot.jsp" %>
