<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Placement Management System | Home</title>
    <!-- Bootstrap 5.3 via CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
            <i class="bi bi-briefcase-fill me-2 text-warning"></i>Placement Management System
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu"
                aria-controls="navMenu" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link active" href="#">Home</a></li>
                <li class="nav-item"><a class="nav-link" href="#features">Features</a></li>
                <li class="nav-item"><a class="nav-link" href="#about">About</a></li>
                <li class="nav-item"><a class="nav-link" href="#contact">Contact</a></li>
            </ul>
            <div class="d-flex gap-2 ms-lg-3">
                <a href="student-login.jsp" class="btn btn-outline-light btn-sm">
                    <i class="bi bi-mortarboard me-1"></i>Student Login
                </a>
                <a href="admin-login.jsp" class="btn btn-warning btn-sm">
                    <i class="bi bi-shield-lock me-1"></i>Admin Login
                </a>
            </div>
        </div>
    </div>
</nav>

<!-- Hero -->
<header class="hero-section text-white text-center">
    <div class="container py-5">
        <span class="badge bg-warning text-dark mb-3 px-3 py-2 rounded-pill">
            <i class="bi bi-stars me-1"></i>Jakarta EE 10 &bull; GlassFish 7.1 &bull; MySQL
        </span>
        <h1 class="display-4 fw-bold">Welcome to the<br>Placement Management System</h1>
        <p class="lead col-lg-8 mx-auto mt-3 opacity-75">
            A centralized platform connecting students, placement officers and recruiters.
            Manage drives, applications, schedules and results — all in one place.
        </p>
        <div class="d-flex flex-column flex-sm-row justify-content-center gap-3 mt-4">
            <a href="student-login.jsp" class="btn btn-primary btn-lg px-5 shadow">
                <i class="bi bi-mortarboard-fill me-2"></i>Student Login
            </a>
            <a href="admin-login.jsp" class="btn btn-warning btn-lg px-5 shadow">
                <i class="bi bi-person-gear me-2"></i>Admin Login
            </a>
        </div>
        <p class="small mt-3 opacity-50">
            <c:choose>
                <c:when test="${sessionScope.userRole eq 'STUDENT'}">
                    You are logged in — <a href="${pageContext.request.contextPath}/student/dashboard" class="text-warning">open your dashboard</a>.
                </c:when>
                <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                    You are logged in — <a href="${pageContext.request.contextPath}/admin/dashboard" class="text-warning">open the admin console</a>.
                </c:when>
                <c:otherwise>
                    Registrations are open — <a href="${pageContext.request.contextPath}/student-registration.jsp" class="text-warning">create your student account</a>.
                </c:otherwise>
            </c:choose>
        </p>

        <!-- Stats (live values loaded from /api/stats; static numbers are demo fallback) -->
        <p class="small opacity-50 mb-0" id="statsNote" hidden>Live database counts.</p>
        <div class="row g-3 mt-4 justify-content-center">
            <div class="col-6 col-md-3">
                <div class="stat-card">
                    <h3 class="fw-bold mb-0" id="statStudents">500+</h3>
                    <small class="text-uppercase">Students</small>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card">
                    <h3 class="fw-bold mb-0" id="statCompanies">50+</h3>
                    <small class="text-uppercase">Companies</small>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card">
                    <h3 class="fw-bold mb-0" id="statApplications">120+</h3>
                    <small class="text-uppercase">Applications</small>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card">
                    <h3 class="fw-bold mb-0" id="statSelected">95%</h3>
                    <small class="text-uppercase">Selected</small>
                </div>
            </div>
        </div>
    </div>
</header>

<!-- Login cards -->
<section class="container my-5">
    <div class="row g-4">
        <div class="col-md-6">
            <div class="card login-card h-100 border-0 shadow-sm">
                <div class="card-body p-4 text-center">
                    <div class="icon-circle bg-primary-subtle text-primary">
                        <i class="bi bi-mortarboard-fill"></i>
                    </div>
                    <h4 class="fw-bold mt-3">Student Portal</h4>
                    <p class="text-muted">View drives, apply for jobs, track application status, build your profile and download offer letters.</p>
                    <a href="student-login.jsp" class="btn btn-primary w-100">Student Login<i class="bi bi-arrow-right ms-2"></i></a>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card login-card h-100 border-0 shadow-sm">
                <div class="card-body p-4 text-center">
                    <div class="icon-circle bg-warning-subtle text-warning-emphasis">
                        <i class="bi bi-shield-lock-fill"></i>
                    </div>
                    <h4 class="fw-bold mt-3">Admin Portal</h4>
                    <p class="text-muted">Manage students, companies, drives, schedules, results and placement reports from one dashboard.</p>
                    <a href="admin-login.jsp" class="btn btn-warning w-100">Admin Login<i class="bi bi-arrow-right ms-2"></i></a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Features -->
<section id="features" class="bg-light py-5">
    <div class="container">
        <h2 class="text-center fw-bold">Why use this platform?</h2>
        <p class="text-center text-muted mb-4">Everything the placement cell needs, in one responsive web app.</p>
        <div class="row g-4">
            <div class="col-md-4">
                <div class="card h-100 border-0 shadow-sm feature-card">
                    <div class="card-body">
                        <i class="bi bi-megaphone-fill fs-2 text-primary"></i>
                        <h5 class="fw-bold mt-2">Drive Management</h5>
                        <p class="text-muted mb-0">Publish upcoming drives, eligibility criteria, CTC, roles and deadlines.</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100 border-0 shadow-sm feature-card">
                    <div class="card-body">
                        <i class="bi bi-clipboard2-check-fill fs-2 text-success"></i>
                        <h5 class="fw-bold mt-2">Applications &amp; Tracking</h5>
                        <p class="text-muted mb-0">One-click apply, live status: applied, shortlisted, interviewed, selected.</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100 border-0 shadow-sm feature-card">
                    <div class="card-body">
                        <i class="bi bi-bar-chart-fill fs-2 text-warning"></i>
                        <h5 class="fw-bold mt-2">Reports &amp; Analytics</h5>
                        <p class="text-muted mb-0">Branch-wise statistics, company-wise offers and downloadable reports.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- About -->
<section id="about" class="container py-5">
    <div class="row align-items-center g-4">
        <div class="col-lg-6">
            <h2 class="fw-bold">Built for colleges &amp; placement cells</h2>
            <p class="text-muted">This project uses <strong>Java 17, Jakarta Servlets, JSP, JDBC and MySQL</strong>,
                packaged as a <code>WAR</code> and deployed to <strong>GlassFish 7.1.0</strong> from Apache NetBeans.</p>
            <ul class="list-unstyled">
                <li class="mb-2"><i class="bi bi-check-circle-fill text-success me-2"></i>Standard Maven layout, WAR packaging</li>
                <li class="mb-2"><i class="bi bi-check-circle-fill text-success me-2"></i>Responsive Bootstrap 5 UI</li>
                <li class="mb-2"><i class="bi bi-check-circle-fill text-success me-2"></i>MySQL-ready via JDBC (to be wired next)</li>
            </ul>
        </div>
        <div class="col-lg-6">
            <div class="card border-0 shadow">
                <div class="card-body">
                    <h5 class="fw-bold"><i class="bi bi-stack me-2"></i>Tech Stack</h5>
                    <span class="badge text-bg-dark m-1">Java 17</span>
                    <span class="badge text-bg-dark m-1">Jakarta Servlet 6.0</span>
                    <span class="badge text-bg-dark m-1">JSP 3.1</span>
                    <span class="badge text-bg-dark m-1">JDBC</span>
                    <span class="badge text-bg-dark m-1">MySQL 8</span>
                    <span class="badge text-bg-dark m-1">Bootstrap 5</span>
                    <span class="badge text-bg-dark m-1">Maven WAR</span>
                    <span class="badge text-bg-dark m-1">GlassFish 7.1.0</span>
                    <hr>
                    <p class="small text-muted mb-0">Context path: <code>/PlacementManagementSystem</code></p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Footer -->
<footer id="contact" class="bg-dark text-white-50 py-4 mt-2">
    <div class="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
        <span><i class="bi bi-briefcase-fill text-warning me-2"></i>&copy; 2026 Placement Management System</span>
        <span class="small">Java 17 &bull; Jakarta EE 10 &bull; NetBeans &bull; GlassFish 7.1.0</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function () {
    fetch('<%= request.getContextPath() %>/api/stats', {headers: {'Accept': 'application/json'}})
        .then(function (r) { if (!r.ok) { throw new Error('stats unavailable'); } return r.json(); })
        .then(function (d) {
            document.getElementById('statStudents').textContent = d.students;
            document.getElementById('statCompanies').textContent = d.companies;
            document.getElementById('statApplications').textContent = d.applications;
            document.getElementById('statSelected').textContent = d.selected;
            document.getElementById('statsNote').hidden = false;
        })
        .catch(function () {
            var cards = document.querySelectorAll('.stat-card small');
            cards.forEach(function (el) { el.textContent += ' (demo)'; });
        });
})();
</script>
</body>
</html>
