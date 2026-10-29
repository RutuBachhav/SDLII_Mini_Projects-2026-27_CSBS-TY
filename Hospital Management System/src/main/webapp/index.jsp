<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@ include file="WEB-INF/views/_head.jsp" %>
<div class="hero">
    <p class="kicker">Outpatient department</p>
    <h1>Outpatient appointments, without the phone queue.</h1>
    <p>CityCare's portal lets patients find the right doctor, request a time that suits them, and track every visit in one place — while doctors review requests and run their day from a single schedule.</p>
    <div class="actions">
        <a class="btn" href="<%= _ctx %>/register">Create a patient account</a>
        <a class="btn btn-secondary" href="<%= _ctx %>/login">Sign in</a>
    </div>
</div>

<h2 class="section-title">How it works</h2>
<ol class="how">
    <li>
        <strong>Create your account</strong>
        <span>Register once with your name, email and password. Your details stay with your own record.</span>
    </li>
    <li>
        <strong>Find a doctor and request a time</strong>
        <span>Browse by department, open a profile, and pick an open slot inside the doctor's working hours.</span>
    </li>
    <li>
        <strong>Track the visit</strong>
        <span>Requests start as Pending. The doctor confirms, declines or completes them — you can cancel while a visit is still upcoming.</span>
    </li>
</ol>

<h2 class="section-title">For doctors</h2>
<p class="section-sub">Doctor accounts are issued by the hospital. Sign in to review requests, confirm or decline them, keep visit notes, and set your working hours and appointment length.</p>
<p><a class="btn btn-secondary" href="<%= _ctx %>/login">Doctor sign in</a></p>
<%@ include file="WEB-INF/views/_foot.jsp" %>
