<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="selected" required="true" type="java.lang.String" %>
<%@ attribute name="groups" required="true" type="com.foreach.imageserver.admin.viewHelpers.GroupsViewHelper" %>

<c:choose>
    <c:when test="${groups.numberOfGroups > 0}">
        <ul class="list groups">
            <c:forEach items="${groups.list}" var="group">
                <c:choose>
                    <c:when test="${selected == group.name}">
                        <!-- selected group -->
                        <li class="active">
                            <a href="${path.siteRoot}/application/${application.id}/group/${group.id}">${group.name}</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <!-- unselected group -->
                        <li>
                            <a href="${path.siteRoot}/application/${application.id}/group/${group.id}">${group.name}</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
        <p class="empty">No groups defined.</p>
    </c:otherwise>
</c:choose>

