<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Companies | Admin Console</title>
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
    <h4 class="fw-bold">Companies &amp; Drives</h4>
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success mt-2" role="alert"><c:out value="${sessionScope.flashSuccess}"/></div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger mt-2" role="alert"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <div class="card border-0 shadow-sm mt-2"><div class="card-body">
        <h6 class="fw-bold">${empty editCompany ? 'Add Company' : 'Edit Company'}</h6>
        <form action="${pageContext.request.contextPath}/admin/companies" method="post" class="row g-2">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="${empty editCompany ? 'add' : 'update'}">
            <c:if test="${not empty editCompany}">
                <input type="hidden" name="companyId" value="${editCompany.companyId}">
            </c:if>
            <div class="col-md-3"><input type="text" class="form-control form-control-sm" name="companyName" maxlength="100" required placeholder="Company name" value="<c:out value='${editCompany.companyName}'/>"></div>
            <div class="col-md-3"><input type="text" class="form-control form-control-sm" name="jobRole" maxlength="100" required placeholder="Job role" value="<c:out value='${editCompany.jobRole}'/>"></div>
            <div class="col-md-2"><input type="number" step="0.01" min="0" class="form-control form-control-sm" name="packageLpa" placeholder="Package LPA" value="<c:out value='${editCompany.packageLpa}'/>"></div>
            <div class="col-md-2"><input type="number" step="0.01" min="0" max="10" class="form-control form-control-sm" name="minimumCgpa" placeholder="Min CGPA" value="<c:out value='${editCompany.minimumCgpa}'/>"></div>
            <div class="col-md-2"><input type="date" class="form-control form-control-sm" name="applicationDeadline" value="<c:out value='${editCompany.applicationDeadline}'/>"></div>
            <div class="col-md-4"><input type="text" class="form-control form-control-sm" name="eligibleBranches" maxlength="255" placeholder="Eligible branches (comma-separated, blank = ALL)" value="<c:out value='${editCompany.eligibleBranches}'/>"></div>
            <div class="col-md-6"><input type="text" class="form-control form-control-sm" name="description" maxlength="2000" placeholder="Description" value="<c:out value='${editCompany.description}'/>"></div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-dark btn-sm w-100">${empty editCompany ? 'Add' : 'Update'}</button>
            </div>
            <c:if test="${not empty editCompany}">
                <div class="col-md-2"><a href="${pageContext.request.contextPath}/admin/companies" class="btn btn-outline-secondary btn-sm w-100">Cancel</a></div>
            </c:if>
        </form>
    </div></div>

    <div class="card border-0 shadow-sm mt-3"><div class="card-body p-0">
        <div class="table-responsive"><table class="table table-hover mb-0">
            <thead class="table-light"><tr><th>ID</th><th>Company</th><th>Role</th><th>Package</th><th>Min CGPA</th><th>Branches</th><th>Deadline</th><th>Actions</th></tr></thead>
            <tbody>
            <c:forEach var="c" items="${companies}">
                <tr><td><c:out value="${c.companyId}"/></td>
                    <td><strong><c:out value="${c.companyName}"/></strong></td>
                    <td><c:out value="${c.jobRole}"/></td>
                    <td><c:out value="${c.packageLpa}"/></td>
                    <td><c:out value="${c.minimumCgpa}"/></td>
                    <td><small><c:out value="${c.eligibleBranches}"/></small></td>
                    <td><c:out value="${c.applicationDeadline}"/></td>
                    <td class="text-nowrap">
                        <a href="${pageContext.request.contextPath}/admin/companies?edit=${c.companyId}" class="btn btn-sm btn-outline-primary">Edit</a>
                        <form action="${pageContext.request.contextPath}/admin/companies" method="post" class="d-inline"
                              onsubmit="return confirm('Delete this company? Blocked if applications exist.');">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="companyId" value="${c.companyId}">
                            <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                        </form>
                    </td></tr>
            </c:forEach>
            <c:if test="${empty companies}">
                <tr><td colspan="8" class="text-center text-muted p-3">No companies yet. Add the first drive above.</td></tr>
            </c:if>
            </tbody>
        </table></div>
    </div></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
