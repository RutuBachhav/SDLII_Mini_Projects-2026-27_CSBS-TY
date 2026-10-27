<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Registration | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
            <i class="bi bi-briefcase-fill me-2 text-warning"></i>Placement Management System
        </a>
        <div class="ms-auto d-flex gap-2">
            <a href="${pageContext.request.contextPath}/student-login.jsp" class="btn btn-outline-light btn-sm">
                <i class="bi bi-box-arrow-in-right me-1"></i>Student Login
            </a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-light btn-sm">Home</a>
        </div>
    </div>
</nav>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-7">
            <div class="card shadow border-0">
                <div class="card-body p-4 p-md-5">
                    <h3 class="fw-bold text-center">Student Registration</h3>
                    <p class="text-muted text-center mb-4">Create your placement portal account.</p>

                    <c:if test="${not empty success}">
                        <div class="alert alert-success" role="alert">
                            <i class="bi bi-check-circle-fill me-2"></i><c:out value="${success}"/>
                        </div>
                        <div class="text-center mb-3">
                            <a href="${pageContext.request.contextPath}/student-login.jsp" class="btn btn-primary">
                                Go to Student Login<i class="bi bi-arrow-right ms-2"></i>
                            </a>
                        </div>
                    </c:if>

                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger" role="alert">
                            <strong>Please fix the following:</strong>
                            <ul class="mb-0 mt-2">
                                <c:forEach var="e" items="${errors}">
                                    <li><c:out value="${e.value}"/></li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/student-register" method="post" autocomplete="on">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="fullName" class="form-label">Full Name</label>
                                <input type="text" class="form-control" id="fullName" name="fullName"
                                       maxlength="100" required autocomplete="name"
                                       value="<c:out value='${formFullName}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="email" class="form-label">Email</label>
                                <input type="email" class="form-control" id="email" name="email"
                                       maxlength="100" required autocomplete="email"
                                       value="<c:out value='${formEmail}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="rollNumber" class="form-label">Roll Number</label>
                                <input type="text" class="form-control" id="rollNumber" name="rollNumber"
                                       maxlength="30" required
                                       value="<c:out value='${formRollNumber}'/>">
                                <div class="form-text">Letters, digits, /, _ or - only.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="branch" class="form-label">Branch</label>
                                <select class="form-select" id="branch" name="branch" required>
                                    <option value="">Select branch…</option>
                                    <option value="Computer Science" ${formBranch eq 'Computer Science' ? 'selected' : ''}>Computer Science</option>
                                    <option value="Information Technology" ${formBranch eq 'Information Technology' ? 'selected' : ''}>Information Technology</option>
                                    <option value="Electronics &amp; Telecommunication" ${formBranch eq 'Electronics & Telecommunication' ? 'selected' : ''}>Electronics &amp; Telecommunication</option>
                                    <option value="Electrical" ${formBranch eq 'Electrical' ? 'selected' : ''}>Electrical</option>
                                    <option value="Mechanical" ${formBranch eq 'Mechanical' ? 'selected' : ''}>Mechanical</option>
                                    <option value="Civil" ${formBranch eq 'Civil' ? 'selected' : ''}>Civil</option>
                                    <option value="Other" ${formBranch eq 'Other' ? 'selected' : ''}>Other</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="cgpa" class="form-label">CGPA (0 – 10)</label>
                                <input type="number" class="form-control" id="cgpa" name="cgpa"
                                       min="0" max="10" step="0.01" required
                                       value="<c:out value='${formCgpa}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="graduationYear" class="form-label">Graduation Year</label>
                                <input type="number" class="form-control" id="graduationYear" name="graduationYear"
                                       min="2000" max="2100" required
                                       value="<c:out value='${formGraduationYear}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" class="form-control" id="password" name="password"
                                       minlength="8" maxlength="128" required autocomplete="new-password">
                                <div class="form-text">Minimum 8 characters.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="confirmPassword" class="form-label">Confirm Password</label>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                       minlength="8" maxlength="128" required autocomplete="new-password">
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 mt-4">
                            <i class="bi bi-person-plus-fill me-2"></i>Register
                        </button>
                    </form>

                    <p class="text-center mt-3 mb-0">
                        Already registered?
                        <a href="${pageContext.request.contextPath}/student-login.jsp">Log in here</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
