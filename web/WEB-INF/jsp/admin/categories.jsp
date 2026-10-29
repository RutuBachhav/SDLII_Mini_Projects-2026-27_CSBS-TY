<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="admin" scope="request" />
<c:set var="pageTitle" value="Categories" scope="request" />
<%@ include file="../_head.jspf" %>

<div class="page-head">
    <h1>Categories</h1>
    <p>
        Retiring a category stops it being offered on new report forms. Reports already filed under
        it keep it, so nothing in the history is invalidated.
    </p>
</div>

<div class="tab-row">
    <a href="${ctx}/admin">Overview</a>
    <a href="${ctx}/admin/items">Reports</a>
    <a href="${ctx}/admin/claims">Claims</a>
    <a href="${ctx}/admin/users">Users</a>
    <a class="on" href="${ctx}/admin/categories">Categories</a>
</div>

<div class="row g-3">
    <div class="col-12 col-lg-8">
        <div class="card card-flush">
            <div class="table-scroll">
                <table class="data">
                    <thead><tr><th>Category</th><th class="num">Reports</th><th>State</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${categories}" var="cat">
                        <tr>
                            <td>
                                <form method="post" action="${ctx}/admin/categories"
                                      class="d-flex gap-2 align-items-center">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="id" value="${cat.id}">
                                    <input type="hidden" name="action" value="rename">
                                    <input type="text" name="name" value="${fn:escapeXml(cat.name)}"
                                           maxlength="60" style="max-width:260px">
                                    <button class="btn btn-ghost btn-sm" type="submit">Rename</button>
                                </form>
                            </td>
                            <td class="num">${cat.itemCount}</td>
                            <td>
                                <span class="chip ${cat.active ? 'chip-resolved' : 'chip-closed'}">
                                    ${cat.active ? 'offered' : 'retired'}
                                </span>
                            </td>
                            <td class="actions-cell">
                                <form method="post" action="${ctx}/admin/categories">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="id" value="${cat.id}">
                                    <input type="hidden" name="action" value="${cat.active ? 'retire' : 'restore'}">
                                    <button class="btn ${cat.active ? 'btn-danger' : 'btn-ghost'} btn-sm" type="submit">
                                        ${cat.active ? 'Retire' : 'Offer again'}
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="card">
            <h2>Add a category</h2>
            <form method="post" action="${ctx}/admin/categories" class="mt-2">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="action" value="create">
                <div class="field">
                    <label for="name">Name</label>
                    <input type="text" id="name" name="name" maxlength="60" required
                           placeholder="Umbrellas">
                    <span class="hint">Names are unique.</span>
                </div>
                <button class="btn" type="submit">Add</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="../_foot.jspf" %>
