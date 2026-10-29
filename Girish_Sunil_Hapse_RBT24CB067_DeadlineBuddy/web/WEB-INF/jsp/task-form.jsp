<%--
    One form for both creating and editing a task.

    ${formAction} and ${formTitle} are set by the servlet, and ${taskId} is
    present only when editing. After a rejected submission the servlet puts the
    values back as request attributes, so the page comes back filled in with the
    messages beside the fields that caused them.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${formTitle}"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Tasks</div>
        <h1>${formTitle}</h1>
        <c:if test="${not empty task}">
            <p class="lede">
                Added ${task.createdAtDisplay}<c:if
                    test="${task.updatedAtDisplay ne task.createdAtDisplay}">, last changed
                    ${task.updatedAtDisplay}</c:if>.
            </p>
        </c:if>
    </div>
    <a class="btn btn-quiet" href="<c:url value='/tasks'/>">Back to tasks</a>
</div>

<c:choose>
    <%-- A task cannot exist without a subject, so say so instead of showing an
         empty dropdown that cannot be submitted. --%>
    <c:when test="${needsSubject}">
        <div class="panel">
            <div class="panel-body">
                <div class="empty">
                    <div class="mark">1</div>
                    <h3>Add a subject first</h3>
                    <p>Every task belongs to a subject - that is what gives you per-course
                       progress. It takes a moment.</p>
                    <a class="btn btn-accent btn-sm" href="<c:url value='/subjects/new'/>">
                        Add a subject
                    </a>
                </div>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <div class="panel" style="max-width: 760px">
            <div class="panel-body">
                <form method="post" action="<c:url value='${formAction}'/>" novalidate>
                    <input type="hidden" name="csrf" value="${csrfToken}">
                    <c:if test="${not empty taskId}">
                        <input type="hidden" name="id" value="${taskId}">
                    </c:if>
                    <c:set var="backTo" value="${empty param.returnTo ? '/tasks' : param.returnTo}"/>
                    <input type="hidden" name="returnTo" value="<c:out value='${backTo}'/>">

                    <div class="form-grid">

                        <div class="span-2">
                            <label class="form-label" for="title">Title</label>
                            <input class="form-control ${not empty errors.title ? 'is-invalid' : ''}"
                                   type="text" id="title" name="title" maxlength="150"
                                   value="<c:out value='${title}'/>"
                                   placeholder="Operating systems assignment 2" autofocus required>
                            <c:if test="${not empty errors.title}">
                                <div class="field-error"><c:out value="${errors.title}"/></div>
                            </c:if>
                        </div>

                        <div class="span-2">
                            <label class="form-label" for="description">Description <span
                                    class="text-muted-2">(optional)</span></label>
                            <textarea class="form-control ${not empty errors.description ? 'is-invalid' : ''}"
                                      id="description" name="description" rows="3"
                                      maxlength="4000"
                                      placeholder="What has to be handed in, and anything you need to remember."><c:out
                                      value="${description}"/></textarea>
                            <c:if test="${not empty errors.description}">
                                <div class="field-error"><c:out value="${errors.description}"/></div>
                            </c:if>
                        </div>

                        <div>
                            <label class="form-label" for="subjectId">Subject</label>
                            <select class="form-select ${not empty errors.subjectId ? 'is-invalid' : ''}"
                                    id="subjectId" name="subjectId" required>
                                <option value="">Choose a subject</option>
                                <c:forEach var="s" items="${subjects}">
                                    <option value="${s.id}" ${subjectId eq s.id ? 'selected' : ''}>
                                        <c:out value="${s.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.subjectId}">
                                <div class="field-error"><c:out value="${errors.subjectId}"/></div>
                            </c:if>
                        </div>

                        <div>
                            <label class="form-label" for="taskType">Type</label>
                            <select class="form-select ${not empty errors.type ? 'is-invalid' : ''}"
                                    id="taskType" name="taskType" required>
                                <c:forEach var="t" items="${taskTypes}">
                                    <option value="${t}" ${taskType eq t.name() ? 'selected' : ''}>
                                        ${t.label}
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.type}">
                                <div class="field-error"><c:out value="${errors.type}"/></div>
                            </c:if>
                        </div>

                        <div>
                            <label class="form-label" for="dueDate">Due date</label>
                            <input class="form-control ${not empty errors.dueDate ? 'is-invalid' : ''}"
                                   type="date" id="dueDate" name="dueDate"
                                   value="<c:out value='${dueDate}'/>" required>
                            <c:choose>
                                <c:when test="${not empty errors.dueDate}">
                                    <div class="field-error"><c:out value="${errors.dueDate}"/></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="hint">A date in the past is allowed - it shows up
                                        as overdue.</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div>
                            <label class="form-label" for="dueTime">Due time <span
                                    class="text-muted-2">(optional)</span></label>
                            <input class="form-control ${not empty errors.dueTime ? 'is-invalid' : ''}"
                                   type="time" id="dueTime" name="dueTime"
                                   value="<c:out value='${dueTime}'/>">
                            <c:if test="${not empty errors.dueTime}">
                                <div class="field-error"><c:out value="${errors.dueTime}"/></div>
                            </c:if>
                        </div>

                        <div>
                            <label class="form-label" for="priority">Priority</label>
                            <select class="form-select ${not empty errors.priority ? 'is-invalid' : ''}"
                                    id="priority" name="priority" required>
                                <c:forEach var="p" items="${priorities}">
                                    <option value="${p}" ${priority eq p.name() ? 'selected' : ''}>
                                        ${p.label}
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.priority}">
                                <div class="field-error"><c:out value="${errors.priority}"/></div>
                            </c:if>
                        </div>

                        <div>
                            <label class="form-label" for="status">Status</label>
                            <select class="form-select ${not empty errors.status ? 'is-invalid' : ''}"
                                    id="status" name="status" required>
                                <c:forEach var="st" items="${statuses}">
                                    <option value="${st}" ${status eq st.name() ? 'selected' : ''}>
                                        ${st.label}
                                    </option>
                                </c:forEach>
                            </select>
                            <c:choose>
                                <c:when test="${not empty errors.status}">
                                    <div class="field-error"><c:out value="${errors.status}"/></div>
                                </c:when>
                                <c:when test="${not empty task and task.completed}">
                                    <div class="hint">This task is completed. Set it back to
                                        pending to reopen it.</div>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-4">
                        <button type="submit" class="btn btn-accent">
                            ${empty taskId ? 'Add task' : 'Save changes'}
                        </button>
                        <a class="btn btn-quiet" href="<c:url value='/tasks'/>">Cancel</a>
                        <c:if test="${not empty taskId}">
                            <span class="flex-grow-1"></span>
                            <a class="btn btn-quiet is-danger"
                               href="<c:url value='/tasks/delete'><c:param name='id' value='${taskId}'/></c:url>">
                                Delete task
                            </a>
                        </c:if>
                    </div>
                </form>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="fragments/foot.jspf" %>
