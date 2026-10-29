<%--
    Subject cards with per-subject progress.

    The counts come from one grouped query (SubjectDao.listWithCounts), so this
    page runs the same number of queries whether the student has two subjects or
    twenty.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Subjects"/>
<%@ include file="fragments/head.jspf" %>

<c:if test="${welcome}">
    <div class="alert alert-info mt-3 mb-0">
        Subjects come first: they group your tasks and give you progress per course.
        Add the courses you are taking this term, then start adding deadlines.
    </div>
</c:if>

<div class="page-head">
    <div>
        <div class="eyebrow">Subjects</div>
        <h1>Your subjects</h1>
        <p class="lede">
            <c:choose>
                <c:when test="${empty subjects}">No subjects yet.</c:when>
                <c:otherwise>
                    ${fn:length(subjects)} ${fn:length(subjects) eq 1 ? 'subject' : 'subjects'},
                    ${stats.total} ${stats.total eq 1 ? 'task' : 'tasks'} between them.
                </c:otherwise>
            </c:choose>
        </p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-quiet" href="<c:url value='/dashboard'/>">Dashboard</a>
        <a class="btn btn-accent" href="<c:url value='/subjects/new'/>">New subject</a>
    </div>
</div>

<c:choose>
    <c:when test="${empty subjects}">
        <div class="panel">
            <div class="panel-body">
                <div class="empty">
                    <div class="mark">1</div>
                    <h3>Start with your courses</h3>
                    <p>Add a subject for each course you are tracking - Operating Systems,
                       DBMS, and so on. Tasks are filed under them.</p>
                    <a class="btn btn-accent btn-sm" href="<c:url value='/subjects/new'/>">
                        Add your first subject
                    </a>
                </div>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <div class="subject-grid">
            <c:forEach var="s" items="${subjects}">
                <div class="subject-card">
                    <div class="head">
                        <span class="bar" style="background: ${s.displayColor}"></span>
                        <div class="flex-grow-1">
                            <div class="name"><c:out value="${s.name}"/></div>
                            <div class="numbers">
                                <c:choose>
                                    <c:when test="${s.taskCount eq 0}">No tasks yet</c:when>
                                    <c:otherwise>
                                        ${s.completedCount} of ${s.taskCount} done,
                                        ${s.pendingCount} open
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <span class="chip">${s.progressPercent}%</span>
                    </div>

                    <div class="meter thin ${s.progressPercent eq 100 and s.taskCount gt 0 ? 'is-good' : ''}"
                         role="progressbar" aria-valuenow="${s.progressPercent}"
                         aria-valuemin="0" aria-valuemax="100">
                        <span style="width: ${s.progressPercent}%"></span>
                    </div>

                    <div class="foot">
                        <a href="<c:url value='/tasks'><c:param name='subject' value='${s.id}'/></c:url>">
                            Tasks
                        </a>
                        <a href="<c:url value='/tasks/new'><c:param name='subject' value='${s.id}'/></c:url>">
                            Add task
                        </a>
                        <a href="<c:url value='/subjects/edit'><c:param name='id' value='${s.id}'/></c:url>">
                            Edit
                        </a>
                        <span class="flex-grow-1"></span>
                        <a class="text-danger"
                           href="<c:url value='/subjects/delete'><c:param name='id' value='${s.id}'/></c:url>">
                            Delete
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="fragments/foot.jspf" %>
