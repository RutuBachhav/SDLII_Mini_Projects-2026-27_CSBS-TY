<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.AppUser"%>
<%@page import="com.hospital.module.Patient"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%@ include file="../_head.jsp" %>
<%
    AppUser user = (AppUser) request.getAttribute("user");
    Patient patient = (Patient) request.getAttribute("patient");
    String csrf = SessionUtil.csrf(request);
    // Preserve user input after a validation failure; otherwise show stored values.
    String fullName = pick(request, "fullName", user.getFullName());
    String phone = pick(request, "phone", str(patient.getPhone()));
    String dobRaw = pick(request, "dobRaw", patient.getDateOfBirth() == null ? "" : patient.getDateOfBirth().toString());
    String gender = pick(request, "gender", str(patient.getGender()));
    String address = pick(request, "address", str(patient.getAddress()));
%>
<%! private static String pick(javax.servlet.http.HttpServletRequest r, String k, String fallback) {
        Object v = r.getAttribute(k);
        if (v != null) { return String.valueOf(v); }
        return fallback == null ? "" : fallback;
    }
    private static String str(String v) { return v == null ? "" : v; }
%>
<div class="page-head">
    <h1>My profile</h1>
    <p>Keep your contact details current so the clinic can reach you about your visits.</p>
</div>
<div class="panel mb-m">
    <dl class="record">
        <dt>Account email</dt><dd><%= ValidationUtil.esc(user.getEmail()) %></dd>
        <dt>Patient ID</dt><dd>#<%= patient.getId() %></dd>
    </dl>
</div>
<form class="form" action="<%= _ctx %>/patient/profile" method="post" novalidate>
    <input type="hidden" name="_csrf" value="<%= csrf %>">
    <div class="field">
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" type="text" autocomplete="name" required
               value="<%= ValidationUtil.esc(fullName) %>" aria-describedby="fullName-err"
               <%= request.getAttribute("nameError") != null ? "aria-invalid=\"true\"" : "" %>>
        <% if (request.getAttribute("nameError") != null) { %><p class="error" id="fullName-err"><%= ValidationUtil.esc((String) request.getAttribute("nameError")) %></p><% } %>
    </div>
    <div class="field">
        <label for="phone">Phone</label>
        <input id="phone" name="phone" type="tel" inputmode="tel" autocomplete="tel"
               value="<%= ValidationUtil.esc(phone) %>" aria-describedby="phone-err"
               <%= request.getAttribute("phoneError") != null ? "aria-invalid=\"true\"" : "" %>>
        <% if (request.getAttribute("phoneError") != null) { %><p class="error" id="phone-err"><%= ValidationUtil.esc((String) request.getAttribute("phoneError")) %></p><% } %>
    </div>
    <div class="field">
        <label for="dateOfBirth">Date of birth</label>
        <input id="dateOfBirth" name="dateOfBirth" type="date"
               value="<%= ValidationUtil.esc(dobRaw) %>" aria-describedby="dob-err"
               <%= request.getAttribute("dobError") != null ? "aria-invalid=\"true\"" : "" %>>
        <% if (request.getAttribute("dobError") != null) { %><p class="error" id="dob-err"><%= ValidationUtil.esc((String) request.getAttribute("dobError")) %></p><% } %>
    </div>
    <div class="field">
        <label for="gender">Gender</label>
        <select id="gender" name="gender">
            <option value="">Prefer not to say</option>
            <% String[] opts = {"Female", "Male", "Other"}; %>
            <% for (String o : opts) { %>
                <option value="<%= o %>" <%= o.equals(gender) ? "selected" : "" %>><%= o %></option>
            <% } %>
        </select>
    </div>
    <div class="field">
        <label for="address">Address</label>
        <textarea id="address" name="address" autocomplete="street-address"><%= ValidationUtil.esc(address) %></textarea>
    </div>
    <button class="btn" type="submit">Save changes</button>
</form>
<%@ include file="../_foot.jsp" %>
