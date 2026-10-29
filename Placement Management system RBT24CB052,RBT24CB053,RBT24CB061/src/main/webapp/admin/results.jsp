<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Results | Admin Console</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>.admin-accent{background:linear-gradient(135deg,#212529,#664d03);}</style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark admin-accent shadow">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/admin/dashboard">
            <i class="bi bi-shield-lock-fill me-2 text-warning"></i>Admin Console</a>
        <div class="ms-auto d-flex gap-2 flex-wrap">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-light btn-sm">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/students" class="btn btn-outline-light btn-sm">Students</a>
            <a href="${pageContext.request.contextPath}/admin/companies" class="btn btn-outline-light btn-sm">Companies</a>
            <a href="${pageContext.request.contextPath}/admin/applications" class="btn btn-outline-light btn-sm">Applications</a>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline m-0">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn btn-outline-light btn-sm">Logout</button>
            </form>
        </div>
    </div>
</nav>
<div class="container-fluid px-4 py-4">
    <h4 class="fw-bold">Placement Results</h4>
    <div class="row g-3 mt-1">
        <div class="col-md-4">
            <div class="card border-0 shadow-sm"><div class="card-body">
                <h6 class="fw-bold">By Status</h6>
                <table class="table table-sm mb-0">
                    <c:forEach var="e" items="${byStatus}">
                        <tr><td><c:out value="${e.key}"/></td><td class="text-end fw-bold"><c:out value="${e.value}"/></td></tr>
                    </c:forEach>
                </table>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card border-0 shadow-sm"><div class="card-body">
                <h6 class="fw-bold">Selections by Company</h6>
                <table class="table table-sm mb-0">
                    <c:forEach var="row" items="${byCompany}">
                        <tr><td><c:out value="${row[0]}"/></td><td class="text-end fw-bold"><c:out value="${row[1]}"/></td></tr>
                    </c:forEach>
                    <c:if test="${empty byCompany}">
                        <tr><td class="text-muted">No selections yet.</td></tr>
                    </c:if>
                </table>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card border-0 shadow-sm text-center"><div class="card-body">
                <h6 class="fw-bold">Placement Rate</h6>
                <p class="display-6 fw-bold mb-0">
                    <c:choose>
                        <c:when test="${placementRate ge 0}">${placementRate}%</c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </p>
                <small class="text-muted"><c:out value="${stats.selected}"/> selected / <c:out value="${stats.applications}"/> applications</small>
            </div></div>
        </div>
    </div>
    <div class="card border-0 shadow-sm mt-3"><div class="card-body p-0">
        <div class="table-responsive"><table class="table table-hover mb-0">
            <thead class="table-light"><tr><th>Student</th><th>Company / Role</th><th>Interview</th><th>Final Status</th><th>Remarks</th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${results}">
                <tr><td><c:out value="${r.fullName}"/><br><small class="text-muted"><c:out value="${r.rollNumber}"/></small></td>
                    <td><c:out value="${r.companyName}"/><br><small class="text-muted"><c:out value="${r.jobRole}"/></small></td>
                    <td><c:out value="${r.interviewDate}"/></td>
                    <td><span class="badge ${r.finalStatus eq 'SELECTED' ? 'text-bg-success' : 'text-bg-danger'}"><c:out value="${r.finalStatus}"/></span></td>
                    <td><c:out value="${r.remarks}"/></td></tr>
            </c:forEach>
            <c:if test="${empty results}">
                <tr><td colspan="5" class="text-center text-muted p-3">No results yet. Update application statuses to generate results.</td></tr>
            </c:if>
            </tbody>
        </table></div>
    </div></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
