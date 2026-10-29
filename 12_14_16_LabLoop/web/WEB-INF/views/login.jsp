<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in · LabLoop</title>
    <link rel="stylesheet" href="${ctx}/assets/labloop.css">
</head>
<body>
<div class="login-wrap">
    <section class="login-art">
        <div class="brand"><div class="brand-mark">LL</div><div><b>LabLoop</b><small><c:out value="${institution}" /></small></div></div>
        <div>
            <h1>Every oscilloscope, robot arm and GPU — booked, issued and maintained in one loop.</h1>
            <p>LabLoop replaces the paper issue register and WhatsApp requests in college labs with one system for students, faculty and lab staff.</p>
            <ul>
                <li>Conflict-free booking with approval workflow</li>
                <li>Practical Kits — reserve a full bench setup in one go</li>
                <li>Issue / return desk with overdue tracking</li>
                <li>Fault reports, repairs and calibration reminders</li>
            </ul>
        </div>
        <small style="color:#7f968b">Jakarta EE 10 · EJB · JPA · CDI · JAX-RS · GlassFish 7</small>
    </section>
    <section class="login-form">
        <div class="login-card">
            <h2>Sign in</h2>
            <p style="color:var(--ink-2);margin-bottom:18px">Use your institute email.</p>
            <c:if test="${not empty error}"><div class="flash err"><c:out value="${error}" /></div></c:if>
            <form method="post" action="${ctx}/login" class="form" id="loginForm">
                <input type="hidden" name="next" value="<c:out value='${param.next}'/>">
                <div>
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" required value="<c:out value='${email}'/>" autofocus>
                </div>
                <div>
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <button class="btn" type="submit" style="justify-content:center">Sign in</button>
            </form>
            <div class="demo card" style="padding:12px 14px">
                <b>Demo accounts</b> — password <code>labloop123</code> (click to fill)
                <table>
                    <tr><td>Admin</td><td><button type="button" onclick="fill('admin@labloop.edu')">admin@labloop.edu</button></td></tr>
                    <tr><td>Lab Assistant</td><td><button type="button" onclick="fill('assistant@labloop.edu')">assistant@labloop.edu</button></td></tr>
                    <tr><td>Faculty</td><td><button type="button" onclick="fill('faculty@labloop.edu')">faculty@labloop.edu</button></td></tr>
                    <tr><td>Student</td><td><button type="button" onclick="fill('student@labloop.edu')">student@labloop.edu</button></td></tr>
                </table>
            </div>
        </div>
    </section>
</div>
<script>
    function fill(email) {
        document.getElementById('email').value = email;
        document.getElementById('password').value = 'labloop123';
    }
</script>
</body>
</html>
