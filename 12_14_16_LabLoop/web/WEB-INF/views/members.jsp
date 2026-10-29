<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Members" /><c:set var="nav" value="members" />
<%@ include file="_header.jspf" %>

<div class="page-head"><div><h1>Members</h1><p>Students, faculty and lab staff who can use LabLoop.</p></div></div>

<div class="grid g-side">
    <div class="card">
        <div class="table-wrap"><table class="t">
            <tr><th>Name</th><th>ID</th><th>Department</th><th>Role</th></tr>
            <c:forEach var="u" items="${members}">
                <tr><td><div class="cell-main"><c:out value="${u.fullName}" /></div><div class="cell-sub"><c:out value="${u.email}" /></div></td>
                    <td class="mono"><c:out value="${u.memberId}" /></td>
                    <td class="cell-sub"><c:out value="${u.department}" /></td>
                    <td><span class="badge ${u.staff ? 'b-IN_USE' : ''}">${u.role.label}</span></td></tr>
            </c:forEach>
        </table></div>
    </div>
    <c:if test="${me.admin}">
        <div class="card">
            <div class="card-h"><h2>Add member</h2></div>
            <div class="card-b">
                <form class="form" method="post" action="${ctx}/members/add">
                    <div><label for="fullName">Full name</label><input type="text" id="fullName" name="fullName" value="<c:out value='${form.fullName}'/>" required></div>
                    <div><label for="email">Email</label><input type="email" id="email" name="email" value="<c:out value='${form.email}'/>" required></div>
                    <div class="row">
                        <div><label for="memberId">Roll no. / staff ID</label><input type="text" id="memberId" name="memberId" value="<c:out value='${form.memberId}'/>" required></div>
                        <div><label for="role">Role</label>
                            <select id="role" name="role"><c:forEach var="r" items="${roles}"><option value="${r}" ${r == form.role ? 'selected' : ''}>${r.label}</option></c:forEach></select></div>
                    </div>
                    <div><label for="department">Department</label><input type="text" id="department" name="department" value="<c:out value='${form.department}'/>" required></div>
                    <div><label for="password">Initial password</label><input type="password" id="password" name="password" minlength="6" required></div>
                    <button class="btn" type="submit">Add member</button>
                </form>
            </div>
        </div>
    </c:if>
</div>
<%@ include file="_footer.jspf" %>
