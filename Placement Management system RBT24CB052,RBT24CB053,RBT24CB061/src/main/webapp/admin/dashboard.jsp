<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Dashboard | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>.admin-accent{background:linear-gradient(135deg,#212529,#664d03);}</style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark admin-accent shadow">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/admin/dashboard">
            <i class="bi bi-shield-lock-fill me-2 text-warning"></i>Admin Console
        </a>
        <div class="ms-auto d-flex gap-2 align-items-center flex-wrap">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-warning btn-sm">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/students" class="btn btn-outline-light btn-sm">Students</a>
            <a href="${pageContext.request.contextPath}/admin/companies" class="btn btn-outline-light btn-sm">Companies</a>
            <a href="${pageContext.request.contextPath}/admin/applications" class="btn btn-outline-light btn-sm">Applications</a>
            <a href="${pageContext.request.contextPath}/admin/results" class="btn btn-outline-light btn-sm">Results</a>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline m-0">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn btn-outline-light btn-sm">Logout</button>
            </form>
        </div>
    </div>
</nav>

<div class="container-fluid px-4 py-4">
    <h4 class="fw-bold">Placement Overview</h4>
    <div class="row g-3 mt-1">
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center"><div class="card-body">
                <i class="bi bi-mortarboard-fill fs-3 text-primary"></i>
                <h3 class="fw-bold mb-0"><c:out value="${stats.students}"/></h3><small class="text-muted">STUDENTS</small>
            </div></div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center"><div class="card-body">
                <i class="bi bi-building-fill fs-3 text-secondary"></i>
                <h3 class="fw-bold mb-0"><c:out value="${stats.companies}"/></h3><small class="text-muted">COMPANIES</small>
            </div></div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center"><div class="card-body">
                <i class="bi bi-clipboard2-fill fs-3 text-info"></i>
                <h3 class="fw-bold mb-0"><c:out value="${stats.applications}"/></h3><small class="text-muted">APPLICATIONS</small>
            </div></div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center"><div class="card-body">
                <i class="bi bi-trophy-fill fs-3 text-warning"></i>
                <h3 class="fw-bold mb-0"><c:out value="${stats.selected}"/></h3><small class="text-muted">SELECTED</small>
            </div></div>
        </div>
    </div>

    <div class="row g-3 mt-1">
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm h-100"><div class="card-body">
                <h6 class="fw-bold">Applications by Status</h6>
                <table class="table table-sm mb-0">
                    <c:forEach var="entry" items="${byStatus}">
                        <tr><td><c:out value="${entry.key}"/></td>
                            <td class="text-end fw-bold"><c:out value="${entry.value}"/></td></tr>
                    </c:forEach>
                    <c:if test="${empty byStatus}">
                        <tr><td class="text-muted">No applications yet.</td></tr>
                    </c:if>
                </table>
            </div></div>
        </div>
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm h-100"><div class="card-body">
                <h6 class="fw-bold">Recent Applications</h6>
                <div class="table-responsive"><table class="table table-sm table-hover mb-0">
                    <thead class="table-light"><tr><th>#</th><th>Student</th><th>Company</th><th>Status</th></tr></thead>
                    <tbody>
                    <c:forEach var="a" items="${recent}">
                        <tr><td><c:out value="${a.applicationId}"/></td>
                            <td><c:out value="${a.fullName}"/> <small class="text-muted">(<c:out value="${a.rollNumber}"/>)</small></td>
                            <td><c:out value="${a.companyName}"/></td>
                            <td><span class="badge text-bg-info"><c:out value="${a.status}"/></span></td></tr>
                    </c:forEach>
                    <c:if test="${empty recent}">
                        <tr><td colspan="4" class="text-muted">No applications yet.</td></tr>
                    </c:if>
                    </tbody>
                </table></div>
                <a href="${pageContext.request.contextPath}/admin/applications" class="btn btn-sm btn-outline-dark mt-3">Manage all applications</a>
            </div></div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
