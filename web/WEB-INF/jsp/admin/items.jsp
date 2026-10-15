<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="admin" scope="request" />
<c:set var="pageTitle" value="Moderate reports" scope="request" />
<%@ include file="../_head.jspf" %>

<div class="page-head">
    <h1>Reports</h1>
    <p>
        Closing a report takes it out of search and matching and keeps every row: the report, its
        claims and its history stay in the database.
    </p>
</div>

<div class="tab-row">
    <a href="${ctx}/admin">Overview</a>
    <a class="on" href="${ctx}/admin/items">Reports</a>
    <a href="${ctx}/admin/claims">Claims</a>
    <a href="${ctx}/admin/users">Users</a>
    <a href="${ctx}/admin/categories">Categories</a>
</div>

<div class="card">
    <form method="get" action="${ctx}/admin/items">
        <div class="filters">
            <div class="field">
                <label for="q">Keyword</label>
                <input type="search" id="q" name="q" value="${fn:escapeXml(q.keyword)}">
            </div>
            <div class="field">
                <label for="type">Side</label>
                <select id="type" name="type">
                    <option value="" ${empty q.type ? 'selected' : ''}>Both</option>
                    <option value="LOST" ${q.type eq 'LOST' ? 'selected' : ''}>Lost</option>
                    <option value="FOUND" ${q.type eq 'FOUND' ? 'selected' : ''}>Found</option>
                </select>
            </div>
            <div class="field">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="ALL" ${q.status eq 'ALL' ? 'selected' : ''}>Any</option>
                    <option value="OPEN" ${q.status eq 'OPEN' ? 'selected' : ''}>Open</option>
                    <option value="RESOLVED" ${q.status eq 'RESOLVED' ? 'selected' : ''}>Resolved</option>
                    <option value="CLOSED" ${q.status eq 'CLOSED' ? 'selected' : ''}>Closed</option>
                </select>
            </div>
            <div class="field"><button class="btn" type="submit">Filter</button></div>
            <div class="field"><a class="btn btn-ghost" href="${ctx}/admin/items">Clear</a></div>
        </div>
    </form>
</div>

<div class="card card-flush mt-3">
    <div class="table-scroll">
        <table class="data">
            <thead>
                <tr>
                    <th>Report</th><th>Side</th><th>Category</th><th>Reporter</th>
                    <th>Date</th><th class="num">Claims</th><th>Status</th><th></th>
                </tr>
            </thead>
            <tbody>
            <c:if test="${empty results}">
                <tr><td colspan="8" class="hint">No reports match those filters.</td></tr>
            </c:if>
            <c:forEach items="${results}" var="it">
                <tr>
                    <td><a href="${ctx}/items/view?id=${it.id}"><c:out value="${it.title}" /></a></td>
                    <td><span class="chip ${it.lost ? 'chip-lost' : 'chip-found'}">${it.lost ? 'lost' : 'found'}</span></td>
                    <td><c:out value="${it.categoryName}" /></td>
                    <td><c:out value="${it.reporterName}" /></td>
                    <td class="mono">${it.itemDateLabel}</td>
                    <td class="num">${it.openClaimCount}</td>
                    <td><span class="chip chip-${fn:toLowerCase(it.status)}">${fn:toLowerCase(it.status)}</span></td>
                    <td class="actions-cell">
                        <c:choose>
                            <c:when test="${it.status eq 'OPEN'}">
                                <form method="post" action="${ctx}/admin/items">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="id" value="${it.id}">
                                    <input type="hidden" name="action" value="close">
                                    <%-- posted back so the redirect returns to the same filtered view --%>
                                    <input type="hidden" name="q" value="${fn:escapeXml(q.keyword)}">
                                    <input type="hidden" name="type" value="${q.type}">
                                    <input type="hidden" name="status" value="${q.status}">
                                    <input type="hidden" name="page" value="${q.page}">
                                    <button class="btn btn-danger btn-sm" type="submit">Close</button>
                                </form>
                            </c:when>
                            <c:when test="${it.status eq 'CLOSED'}">
                                <form method="post" action="${ctx}/admin/items">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="id" value="${it.id}">
                                    <input type="hidden" name="action" value="reopen">
                                    <%-- posted back so the redirect returns to the same filtered view --%>
                                    <input type="hidden" name="q" value="${fn:escapeXml(q.keyword)}">
                                    <input type="hidden" name="type" value="${q.type}">
                                    <input type="hidden" name="status" value="${q.status}">
                                    <input type="hidden" name="page" value="${q.page}">
                                    <button class="btn btn-ghost btn-sm" type="submit">Reopen</button>
                                </form>
                            </c:when>
                            <c:otherwise><span class="hint">returned</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${pages gt 1}">
    <nav class="pager">
        <c:forEach begin="1" end="${pages}" var="p">
            <c:choose>
                <c:when test="${p eq q.page}"><span class="on">${p}</span></c:when>
                <c:otherwise>
                    <a href="${ctx}/admin/items?q=${fn:escapeXml(q.keyword)}&amp;type=${q.type}&amp;status=${q.status}&amp;page=${p}">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </nav>
</c:if>

<p class="hint mt-3">Showing ${fn:length(results)} of ${total} reports.</p>

<%@ include file="../_foot.jspf" %>
