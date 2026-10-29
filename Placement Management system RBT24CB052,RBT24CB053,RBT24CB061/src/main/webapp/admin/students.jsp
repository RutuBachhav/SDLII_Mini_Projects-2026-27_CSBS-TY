<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Students | Admin Console</title>
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
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h4 class="fw-bold mb-0">Students</h4>
        <form action="${pageContext.request.contextPath}/admin/students" method="get" class="d-flex gap-2">
            <input type="search" class="form-control form-control-sm" name="q" maxlength="100"
                   placeholder="Search name, roll, email, branch" value="<c:out value='${q}'/>">
            <button type="submit" class="btn btn-dark btn-sm">Search</button>
        </form>
    </div>
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success mt-3" role="alert"><c:out value="${sessionScope.flashSuccess}"/></div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger mt-3" role="alert"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>
    <div class="card border-0 shadow-sm mt-3"><div class="card-body p-0">
        <div class="table-responsive"><table class="table table-hover mb-0">
            <thead class="table-light"><tr><th>ID</th><th>Name</th><th>Roll No</th><th>Email</th><th>Branch</th><th>CGPA</th><th>Year</th><th>Phone</th><th>Edit CGPA</th></tr></thead>
            <tbody>
            <c:forEach var="s" items="${students}">
                <tr><td><c:out value="${s.studentId}"/></td>
                    <td><c:out value="${s.fullName}"/></td>
                    <td><c:out value="${s.rollNumber}"/></td>
                    <td><c:out value="${s.email}"/></td>
                    <td><c:out value="${s.branch}"/></td>
                    <td><c:out value="${s.cgpa}"/></td>
                    <td><c:out value="${s.graduationYear}"/></td>
                    <td><c:out value="${s.phone}"/></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin/students" method="post" class="d-flex gap-1">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="updateCgpa">
                            <input type="hidden" name="studentId" value="${s.studentId}">
                            <input type="hidden" name="q" value="<c:out value='${q}'/>">
                            <input type="number" name="cgpa" class="form-control form-control-sm"
                                   min="0" max="10" step="0.01" required style="width:5.5rem"
                                   value="<c:out value='${s.cgpa}'/>" aria-label="CGPA for ${s.rollNumber}">
                            <button type="submit" class="btn btn-sm btn-dark">Save</button>
                        </form>
                    </td></tr>
            </c:forEach>
            <c:if test="${empty students}">
                <tr><td colspan="9" class="text-center text-muted p-3">No students found.</td></tr>
            </c:if>
            </tbody>
        </table></div>
    </div></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
