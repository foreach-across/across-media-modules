<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="selected" required="true" type="java.lang.String" %>
<%@ attribute name="formats" required="true" type="com.foreach.imageserver.admin.viewHelpers.FormatsViewHelper" %>

<c:choose>
    <c:when test="${formats.numberOfFormats > 0}">
        <ul class="list formats">
            <c:forEach items="${formats.list}" var="format">
                <li>
                    dimension : ${format.dimensions.width} x ${format.dimensions.height} | ratio : ${format.dimensions.aspectRatio}
                    <span class="actions"><a href="${path.siteRoot}/application/${application.id}/group/${group.id}/format/${format.id}">edit</a></span>
                </li>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
       <p class="empty">No formats found.</p>
    </c:otherwise>
</c:choose>

