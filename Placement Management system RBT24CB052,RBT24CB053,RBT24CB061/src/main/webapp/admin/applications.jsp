<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Applications | Admin Console</title>
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
            <a href="${pageContext.request.contextPath}/admin/results" class="btn btn-outline-light btn-sm">Results</a>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline m-0">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn btn-outline-light btn-sm">Logout</button>
            </form>
        </div>
    </div>
</nav>
<div class="container-fluid px-4 py-4">
    <h4 class="fw-bold">Applications</h4>
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success mt-2" role="alert"><c:out value="${sessionScope.flashSuccess}"/></div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger mt-2" role="alert"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>
    <form action="${pageContext.request.contextPath}/admin/applications" method="get" class="row g-2 mt-1 align-items-end">
        <div class="col-md-3">
            <label class="form-label small">Status</label>
            <select name="status" class="form-select form-select-sm">
                <option value="">All statuses</option>
                <c:forEach var="s" items="${statuses}">
                    <option value="${s}" ${filterStatus eq s ? 'selected' : ''}>${s}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-4">
            <label class="form-label small">Company</label>
            <select name="companyId" class="form-select form-select-sm">
                <option value="">All companies</option>
                <c:forEach var="c" items="${companies}">
                    <option value="${c.companyId}" ${filterCompany eq c.companyId ? 'selected' : ''}><c:out value="${c.companyName}"/> — <c:out value="${c.jobRole}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2"><button type="submit" class="btn btn-dark btn-sm">Filter</button></div>
    </form>

    <div class="card border-0 shadow-sm mt-3"><div class="card-body p-0">
        <div class="table-responsive"><table class="table table-hover mb-0">
            <thead class="table-light"><tr><th>#</th><th>Student</th><th>Company / Role</th><th>Applied</th><th>Update Status</th></tr></thead>
            <tbody>
            <c:forEach var="a" items="${applications}">
                <tr><td><c:out value="${a.applicationId}"/></td>
                    <td><c:out value="${a.fullName}"/><br><small class="text-muted"><c:out value="${a.rollNumber}"/> · <c:out value="${a.branch}"/></small></td>
                    <td><c:out value="${a.companyName}"/><br><small class="text-muted"><c:out value="${a.jobRole}"/></small></td>
                    <td><c:out value="${a.applicationDate}"/><br><span class="badge text-bg-info"><c:out value="${a.status}"/></span></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin/applications" method="post" class="row g-1 align-items-end">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="applicationId" value="${a.applicationId}">
                            <div class="col-12 col-xl-4">
                                <select name="status" class="form-select form-select-sm">
                                    <c:forEach var="s" items="${statuses}">
                                        <option value="${s}" ${a.status eq s ? 'selected' : ''}>${s}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-6 col-xl-3"><input type="date" name="interviewDate" class="form-control form-control-sm" value="<c:out value='${a.interviewDate}'/>"></div>
                            <div class="col-6 col-xl-3"><input type="text" name="remarks" maxlength="500" class="form-control form-control-sm" placeholder="Remarks" value="<c:out value='${a.remarks}'/>"></div>
                            <div class="col-12 col-xl-2"><button type="submit" class="btn btn-sm btn-dark w-100">Save</button></div>
                        </form>
                    </td></tr>
            </c:forEach>
            <c:if test="${empty applications}">
                <tr><td colspan="5" class="text-center text-muted p-3">No applications match the filter.</td></tr>
            </c:if>
            </tbody>
        </table></div>
    </div></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
