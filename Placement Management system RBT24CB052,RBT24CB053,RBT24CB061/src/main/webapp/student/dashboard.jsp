<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Dashboard | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>.student-accent{background:linear-gradient(135deg,#0d6efd,#084298);}</style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark student-accent shadow">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/student/dashboard">
            <i class="bi bi-mortarboard-fill me-2"></i>Student Portal
        </a>
        <div class="ms-auto d-flex gap-2 align-items-center">
            <span class="text-white-50 small d-none d-md-inline"><c:out value="${sessionScope.userEmail}"/></span>
            <a href="${pageContext.request.contextPath}/student/dashboard" class="btn btn-light btn-sm">Dashboard</a>
            <a href="${pageContext.request.contextPath}/student/profile" class="btn btn-outline-light btn-sm">Profile</a>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline m-0">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn btn-outline-light btn-sm">Logout</button>
            </form>
        </div>
    </div>
</nav>

<div class="container py-4">
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success" role="alert"><c:out value="${sessionScope.flashSuccess}"/></div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger" role="alert"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm h-100">
                <div class="card-body">
                    <h5 class="fw-bold"><i class="bi bi-person-circle me-2 text-primary"></i>My Profile</h5>
                    <dl class="row mb-0 small">
                        <dt class="col-5">Name</dt><dd class="col-7"><c:out value="${student.fullName}"/></dd>
                        <dt class="col-5">Roll No</dt><dd class="col-7"><c:out value="${student.rollNumber}"/></dd>
                        <dt class="col-5">Branch</dt><dd class="col-7"><c:out value="${student.branch}"/></dd>
                        <dt class="col-5">CGPA</dt><dd class="col-7"><c:out value="${student.cgpa}"/></dd>
                        <dt class="col-5">Grad. Year</dt><dd class="col-7"><c:out value="${student.graduationYear}"/></dd>
                        <dt class="col-5">Phone</dt><dd class="col-7"><c:out value="${student.phone}"/></dd>
                        <dt class="col-5">Skills</dt><dd class="col-7"><c:out value="${student.skills}"/></dd>
                    </dl>
                    <a href="${pageContext.request.contextPath}/student/profile" class="btn btn-outline-primary btn-sm w-100 mt-3">Edit Profile</a>
                </div>
            </div>
        </div>
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm">
                <div class="card-body">
                    <h5 class="fw-bold"><i class="bi bi-megaphone-fill me-2 text-primary"></i>Available Drives</h5>
                    <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                        <tr><th>Company</th><th>Role</th><th>Package</th><th>Min CGPA</th><th>Deadline</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${companyRows}">
                            <tr>
                                <td><strong><c:out value="${row.company.companyName}"/></strong><br>
                                    <small class="text-muted"><c:out value="${row.company.eligibleBranches}"/></small></td>
                                <td><c:out value="${row.company.jobRole}"/></td>
                                <td><c:out value="${row.company.packageLpa}"/> LPA</td>
                                <td><c:out value="${row.company.minimumCgpa}"/></td>
                                <td><c:out value="${row.company.applicationDeadline}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${row.applied}"><span class="badge text-bg-success">Applied</span></c:when>
                                        <c:when test="${row.eligible}">
                                            <form action="${pageContext.request.contextPath}/student/apply" method="post" class="m-0">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="companyId" value="${row.company.companyId}">
                                                <button type="submit" class="btn btn-primary btn-sm">Apply</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise><span class="badge text-bg-secondary"><c:out value="${row.reason}"/></span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty companyRows}">
                            <tr><td colspan="6" class="text-center text-muted">No drives posted yet.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card border-0 shadow-sm mt-4">
        <div class="card-body">
            <h5 class="fw-bold"><i class="bi bi-clipboard2-check-fill me-2 text-success"></i>My Applications</h5>
            <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr><th>Company</th><th>Role</th><th>Applied On</th><th>Status</th><th>Interview</th><th>Result</th><th>Remarks</th></tr>
                </thead>
                <tbody>
                <c:forEach var="a" items="${applications}">
                    <tr>
                        <td><c:out value="${a.companyName}"/></td>
                        <td><c:out value="${a.jobRole}"/></td>
                        <td><c:out value="${a.applicationDate}"/></td>
                        <td><span class="badge text-bg-info"><c:out value="${a.status}"/></span></td>
                        <td><c:out value="${a.interviewDate}"/></td>
                        <td>
                            <c:if test="${not empty a.finalStatus}">
                                <span class="badge ${a.finalStatus eq 'SELECTED' ? 'text-bg-success' : 'text-bg-danger'}"><c:out value="${a.finalStatus}"/></span>
                            </c:if>
                        </td>
                        <td><c:out value="${a.remarks}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty applications}">
                    <tr><td colspan="7" class="text-center text-muted">You have not applied to any company yet.</td></tr>
                </c:if>
                </tbody>
            </table>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
