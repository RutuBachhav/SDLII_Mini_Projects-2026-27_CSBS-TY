<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Something went wrong" scope="request" />
<%@ include file="_head.jspf" %>

<div class="row justify-content-center">
    <div class="col-12 col-md-8">
        <div class="empty" style="text-align:left">
            <h1>${empty errorTitle ? 'That page could not be shown' : errorTitle}</h1>
            <p style="margin-left:0;max-width:60ch">
                <c:out value="${empty errorDetail
                    ? 'The address may be wrong, or the record may have been removed. Nothing was changed.'
                    : errorDetail}" />
            </p>
            <div class="actions">
                <a class="btn" href="${ctx}/home">Back to the start</a>
                <a class="btn btn-ghost" href="${ctx}/browse">Browse reports</a>
            </div>
        </div>
    </div>
</div>

<%@ include file="_foot.jspf" %>
