<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Error | Placement Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow border-0">
                <div class="card-body p-4 text-center">
                    <h3 class="fw-bold">Something went wrong</h3>
                    <p class="text-muted">
                        <c:choose>
                            <c:when test="${not empty pageError}"><c:out value="${pageError}"/></c:when>
                            <c:otherwise>An unexpected error occurred. Please try again later.</c:otherwise>
                        </c:choose>
                    </p>
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Back to Home</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
