<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Setup | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card shadow border-0">
                <div class="card-body p-4">
                    <h3 class="fw-bold text-center">One-Time Admin Setup</h3>
                    <p class="text-muted text-center small">Requires the server setup token. Disabled after the first admin exists.</p>
                    <c:if test="${not empty setupError}">
                        <div class="alert alert-danger" role="alert"><c:out value="${setupError}"/></div>
                    </c:if>
                    <c:if test="${not empty setupSuccess}">
                        <div class="alert alert-success" role="alert"><c:out value="${setupSuccess}"/></div>
                        <div class="text-center mb-2">
                            <a href="${pageContext.request.contextPath}/admin-login.jsp" class="btn btn-warning">Go to Admin Login</a>
                        </div>
                    </c:if>
                    <form action="${pageContext.request.contextPath}/admin-setup" method="post">
                        <div class="mb-3">
                            <label for="email" class="form-label">Admin Email</label>
                            <input type="email" class="form-control" id="email" name="email" maxlength="100" required autocomplete="off">
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password (min 8 chars)</label>
                            <input type="password" class="form-control" id="password" name="password" minlength="8" maxlength="128" required autocomplete="new-password">
                        </div>
                        <div class="mb-3">
                            <label for="setupToken" class="form-label">Setup Token</label>
                            <input type="password" class="form-control" id="setupToken" name="setupToken" required autocomplete="off">
                        </div>
                        <button type="submit" class="btn btn-dark w-100">Create Admin</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
