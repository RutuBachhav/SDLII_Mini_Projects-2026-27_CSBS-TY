<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Profile | Student Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>.student-accent{background:linear-gradient(135deg,#0d6efd,#084298);}</style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark student-accent shadow">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/student/dashboard">
            <i class="bi bi-mortarboard-fill me-2"></i>Student Portal
        </a>
        <div class="ms-auto d-flex gap-2">
            <a href="${pageContext.request.contextPath}/student/dashboard" class="btn btn-outline-light btn-sm">Dashboard</a>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline m-0">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn btn-outline-light btn-sm">Logout</button>
            </form>
        </div>
    </div>
</nav>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card border-0 shadow-sm">
                <div class="card-body p-4">
                    <h5 class="fw-bold">Edit Profile</h5>
                    <p class="text-muted small">Branch and graduation year are verified records — contact the placement cell to change them.</p>
                    <c:if test="${not empty success}">
                        <div class="alert alert-success" role="alert"><c:out value="${success}"/></div>
                    </c:if>
                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger" role="alert">
                            <ul class="mb-0">
                                <c:forEach var="e" items="${errors}"><li><c:out value="${e.value}"/></li></c:forEach>
                            </ul>
                        </div>
                    </c:if>
                    <dl class="row small bg-light rounded p-2">
                        <dt class="col-5">Roll No</dt><dd class="col-7"><c:out value="${student.rollNumber}"/></dd>
                        <dt class="col-5">Branch</dt><dd class="col-7"><c:out value="${student.branch}"/></dd>
                        <dt class="col-5">Grad. Year</dt><dd class="col-7"><c:out value="${student.graduationYear}"/></dd>
                    </dl>
                    <form action="${pageContext.request.contextPath}/student/profile" method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <div class="mb-3">
                            <label for="fullName" class="form-label">Full Name</label>
                            <input type="text" class="form-control" id="fullName" name="fullName"
                                   maxlength="100" required value="<c:out value='${student.fullName}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="phone" class="form-label">Phone</label>
                            <input type="text" class="form-control" id="phone" name="phone"
                                   maxlength="15" value="<c:out value='${student.phone}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="cgpa" class="form-label">CGPA (0 – 10)</label>
                            <input type="number" class="form-control" id="cgpa" name="cgpa"
                                   min="0" max="10" step="0.01" required
                                   value="<c:out value='${student.cgpa}'/>">
                            <div class="form-text">Decimal value with at most two decimal places.</div>
                        </div>
                        <div class="mb-3">
                            <label for="skills" class="form-label">Skills</label>
                            <textarea class="form-control" id="skills" name="skills" rows="3" maxlength="2000"><c:out value="${student.skills}"/></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">Save Changes</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
