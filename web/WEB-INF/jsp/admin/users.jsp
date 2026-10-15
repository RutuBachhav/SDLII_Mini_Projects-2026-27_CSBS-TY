<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="admin" scope="request" />
<c:set var="pageTitle" value="Users" scope="request" />
<%@ include file="../_head.jspf" %>

<div class="page-head">
    <h1>Users</h1>
    <p>
        Accounts are suspended, never deleted, so their reports, claims and history stay intact and
        the foreign keys stay valid. A suspended account cannot sign in.
    </p>
</div>

<div class="tab-row">
    <a href="${ctx}/admin">Overview</a>
    <a href="${ctx}/admin/items">Reports</a>
    <a href="${ctx}/admin/claims">Claims</a>
    <a class="on" href="${ctx}/admin/users">Users</a>
    <a href="${ctx}/admin/categories">Categories</a>
</div>

<div class="card">
    <form method="get" action="${ctx}/admin/users" class="filters">
        <div class="field">
            <label for="q">Search by name or email</label>
            <input type="search" id="q" name="q" value="${fn:escapeXml(keyword)}">
        </div>
        <div class="field"><button class="btn" type="submit">Search</button></div>
        <div class="field"><a class="btn btn-ghost" href="${ctx}/admin/users">Clear</a></div>
    </form>
</div>

<div class="card card-flush mt-3">
    <div class="table-scroll">
        <table class="data">
            <thead>
                <tr><th>Name</th><th>Email</th><th>Role</th><th class="num">Reports</th>
                    <th>Joined</th><th>State</th><th></th></tr>
            </thead>
            <tbody>
            <c:if test="${empty users}">
                <tr><td colspan="7" class="hint">No accounts match that search.</td></tr>
            </c:if>
            <c:forEach items="${users}" var="u">
                <tr>
                    <td class="strong"><c:out value="${u.name}" /></td>
                    <td class="mono"><c:out value="${u.email}" /></td>
                    <td>
                        <c:choose>
                            <c:when test="${u.admin}"><span class="chip chip-admin">admin</span></c:when>
                            <c:otherwise><span class="hint">user</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="num">${u.reportCount}</td>
                    <td class="mono">${u.createdLabel}</td>
                    <td>
                        <span class="chip ${u.active ? 'chip-resolved' : 'chip-closed'}">
                            ${u.active ? 'active' : 'suspended'}
                        </span>
                    </td>
                    <td class="actions-cell">
                        <c:choose>
                            <c:when test="${u.admin or u.id eq user.id}">
                                <span class="hint">&mdash;</span>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/admin/users">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <input type="hidden" name="action" value="${u.active ? 'suspend' : 'activate'}">
                                    <button class="btn ${u.active ? 'btn-danger' : 'btn-ghost'} btn-sm" type="submit">
                                        ${u.active ? 'Suspend' : 'Reactivate'}
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<p class="hint mt-3">
    The administrator role is not granted through the web interface. It is set once, on the command
    line, with the AdminInit tool described in the README.
</p>

<%@ include file="../_foot.jspf" %>
