<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="browse" scope="request" />
<c:set var="pageTitle" value="Browse reports" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <div class="row-between">
        <div>
            <h1>Browse reports</h1>
            <p>
                <span class="mono">${total}</span>
                ${total eq 1 ? 'report' : 'reports'} match your filters.
            </p>
        </div>
        <div class="actions" style="margin:0">
            <a class="btn btn-ghost btn-sm" href="${ctx}/report?type=LOST">Report lost</a>
            <a class="btn btn-sm" href="${ctx}/report?type=FOUND">Report found</a>
        </div>
    </div>
</div>

<div class="card">
    <form method="get" action="${ctx}/browse">
        <div class="filters">
            <div class="field">
                <label for="q">Keyword</label>
                <input type="search" id="q" name="q" value="${fn:escapeXml(q.keyword)}"
                       placeholder="wallet, calculator, keys">
            </div>
            <div class="field">
                <label for="type">Side</label>
                <select id="type" name="type">
                    <option value="" ${empty q.type ? 'selected' : ''}>Lost and found</option>
                    <option value="LOST" ${q.type eq 'LOST' ? 'selected' : ''}>Lost only</option>
                    <option value="FOUND" ${q.type eq 'FOUND' ? 'selected' : ''}>Found only</option>
                </select>
            </div>
            <div class="field">
                <label for="category">Category</label>
                <select id="category" name="category">
                    <option value="0">All categories</option>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${q.categoryId eq cat.id ? 'selected' : ''}>
                            <c:out value="${cat.name}" />
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="OPEN" ${q.status eq 'OPEN' ? 'selected' : ''}>Open</option>
                    <option value="RESOLVED" ${q.status eq 'RESOLVED' ? 'selected' : ''}>Resolved</option>
                    <option value="CLOSED" ${q.status eq 'CLOSED' ? 'selected' : ''}>Closed</option>
                    <option value="ALL" ${q.status eq 'ALL' ? 'selected' : ''}>Any status</option>
                </select>
            </div>
            <div class="field">
                <button class="btn" type="submit">Apply</button>
            </div>
        </div>
        <div class="filters mt-3">
            <div class="field">
                <label for="location">Location</label>
                <input list="known-locations" id="location" name="location"
                       value="${fn:escapeXml(q.location)}" placeholder="Library, canteen, lab">
                <datalist id="known-locations">
                    <c:forEach items="${locations}" var="loc">
                        <option value="${fn:escapeXml(loc)}"></option>
                    </c:forEach>
                </datalist>
            </div>
            <div class="field">
                <label for="from">Date from</label>
                <input type="date" id="from" name="from" value="${q.dateFrom}">
            </div>
            <div class="field">
                <label for="to">Date to</label>
                <input type="date" id="to" name="to" value="${q.dateTo}">
            </div>
            <div class="field">
                <label for="sort">Order</label>
                <select id="sort" name="sort">
                    <option value="newest" ${q.sort eq 'newest' ? 'selected' : ''}>Newest first</option>
                    <option value="oldest" ${q.sort eq 'oldest' ? 'selected' : ''}>Oldest first</option>
                </select>
            </div>
            <div class="field">
                <a class="btn btn-ghost" href="${ctx}/browse">Clear</a>
            </div>
        </div>
    </form>
</div>

<section class="section">
    <c:choose>
        <c:when test="${empty results}">
            <div class="empty">
                <h3>No reports match those filters</h3>
                <p>
                    Try a single word instead of a phrase, widen the dates, or set the status to
                    any status. Items that were returned are kept, so they are there under
                    <em>resolved</em>.
                </p>
                <div class="actions">
                    <a class="btn btn-ghost" href="${ctx}/browse">Clear filters</a>
                    <a class="btn" href="${ctx}/report?type=LOST">Report a lost item</a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="item-grid">
                <c:forEach items="${results}" var="it">
                    <%@ include file="_itemcard.jspf" %>
                </c:forEach>
            </div>

            <c:if test="${pages gt 1}">
                <c:url var="base" value="/browse">
                    <c:param name="q" value="${q.keyword}" />
                    <c:param name="type" value="${q.type}" />
                    <c:param name="category" value="${q.categoryId}" />
                    <c:param name="location" value="${q.location}" />
                    <c:param name="status" value="${q.status}" />
                    <c:param name="from" value="${q.dateFrom}" />
                    <c:param name="to" value="${q.dateTo}" />
                    <c:param name="sort" value="${q.sort}" />
                </c:url>
                <nav class="pager" aria-label="Pages">
                    <c:if test="${q.page gt 1}">
                        <a href="${base}&amp;page=${q.page - 1}">Previous</a>
                    </c:if>
                    <c:forEach begin="1" end="${pages}" var="p">
                        <c:choose>
                            <c:when test="${p eq q.page}"><span class="on">${p}</span></c:when>
                            <c:when test="${p le 2 or p gt pages - 2 or (p ge q.page - 1 and p le q.page + 1)}">
                                <a href="${base}&amp;page=${p}">${p}</a>
                            </c:when>
                            <c:when test="${p eq 3 or p eq pages - 2}"><span class="gap">&hellip;</span></c:when>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${q.page lt pages}">
                        <a href="${base}&amp;page=${q.page + 1}">Next</a>
                    </c:if>
                </nav>
            </c:if>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="_foot.jspf" %>
