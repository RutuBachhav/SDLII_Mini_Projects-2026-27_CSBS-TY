<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@page import="com.hospital.web.SessionUtil"%>
<%
    String _title = (String) request.getAttribute("pageTitle");
    if (_title == null || _title.trim().isEmpty()) { _title = "Hospital"; }
    String _ctx = request.getContextPath();
    String _role = (String) session.getAttribute("urole");
    String _uname = (String) session.getAttribute("uname");
    boolean _patient = "PATIENT".equals(_role);
    boolean _doctor = "DOCTOR".equals(_role);
    String _path = request.getServletPath();
    if (_path == null) { _path = ""; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= ValidationUtil.esc(_title) %> — CityCare Hospital</title>
    <meta name="description" content="CityCare Hospital patient and doctor portal: appointments, schedules and profiles.">
    <meta name="color-scheme" content="light">
    <meta name="theme-color" content="#fdfcf8">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= _ctx %>/assets/styles.css">
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<header class="site-header">
    <div class="wrap">
        <a class="brand" href="<%= _ctx %>/">CityCare <span>Hospital</span></a>
        <nav class="site-nav" aria-label="Primary">
            <% if (_patient) { %>
                <a href="<%= _ctx %>/patient/dashboard" <%= _path.equals("/patient/dashboard") ? "aria-current=\"page\"" : "" %>>Dashboard</a>
                <a href="<%= _ctx %>/patient/doctors" <%= _path.startsWith("/patient/doctor") ? "aria-current=\"page\"" : "" %>>Find doctors</a>
                <a href="<%= _ctx %>/patient/appointments" <%= _path.equals("/patient/appointments") ? "aria-current=\"page\"" : "" %>>My appointments</a>
                <a href="<%= _ctx %>/patient/profile" <%= _path.equals("/patient/profile") ? "aria-current=\"page\"" : "" %>>Profile</a>
            <% } else if (_doctor) { %>
                <a href="<%= _ctx %>/doctor/dashboard" <%= _path.equals("/doctor/dashboard") ? "aria-current=\"page\"" : "" %>>Dashboard</a>
                <a href="<%= _ctx %>/doctor/appointments" <%= _path.startsWith("/doctor/appointments") ? "aria-current=\"page\"" : "" %>>Schedule</a>
                <a href="<%= _ctx %>/doctor/profile" <%= _path.equals("/doctor/profile") ? "aria-current=\"page\"" : "" %>>Profile</a>
            <% } else { %>
                <a href="<%= _ctx %>/login">Sign in</a>
                <a href="<%= _ctx %>/register">Create account</a>
            <% } %>
        </nav>
        <div class="header-user">
            <% if (_uname != null) { %>
                <span>Signed in as <strong><%= ValidationUtil.esc(_uname) %></strong></span>
                <a class="btn btn-secondary btn-small" href="<%= _ctx %>/logout">Sign out</a>
            <% } %>
        </div>
    </div>
</header>
<main id="main" tabindex="-1">
    <div class="wrap">
        <div aria-live="polite" aria-atomic="true" class="sr-only" id="status-region"></div>
        <% String _notice = (String) request.getAttribute("notice"); %>
        <% if (_notice != null && !_notice.isEmpty()) { %>
            <div class="notice notice-success" role="status">
                <p><span class="tag">Done — </span><%= ValidationUtil.esc(_notice) %></p>
                <button type="button" class="dismiss" data-dismiss aria-label="Dismiss message">&times;</button>
            </div>
        <% } %>
        <% String _formError = (String) request.getAttribute("formError"); %>
        <% if (_formError != null && !_formError.isEmpty()) { %>
            <div class="notice notice-error" role="alert">
                <p><span class="tag">Something needs attention — </span><%= ValidationUtil.esc(_formError) %></p>
                <button type="button" class="dismiss" data-dismiss aria-label="Dismiss message">&times;</button>
            </div>
        <% } %>
