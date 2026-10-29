<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Login | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card shadow border-0 border-top border-warning border-4">
                <div class="card-body p-4">
                    <h3 class="fw-bold text-center"><i class="bi bi-shield-lock-fill me-2 text-warning"></i>Admin Login</h3>
                    <c:if test="${not empty sessionScope.flashError}">
                        <div class="alert alert-warning mt-3" role="alert"><c:out value="${sessionScope.flashError}"/></div>
                        <c:remove var="flashError" scope="session"/>
                    </c:if>
                    <c:if test="${not empty loginError}">
                        <div class="alert alert-danger mt-3" role="alert"><c:out value="${loginError}"/></div>
                    </c:if>
                    <form action="${pageContext.request.contextPath}/admin-login" method="post" class="mt-3">
                        <div class="mb-3">
                            <label for="email" class="form-label">Admin Email</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   maxlength="100" required autocomplete="username"
                                   value="<c:out value='${formEmail}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="password" name="password"
                                   maxlength="128" required autocomplete="current-password">
                        </div>
                        <button type="submit" class="btn btn-warning w-100">Log In as Admin</button>
                    </form>
                    <p class="text-center mt-3 mb-0"><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
