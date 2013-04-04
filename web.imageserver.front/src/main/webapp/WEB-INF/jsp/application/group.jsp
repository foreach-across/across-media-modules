<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="list" tagdir="/WEB-INF/tags/list" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<t:page title="${group.name}">
	<t:body>
        <div id="app-wrapper">
            <ul class="meta">
                <li class="title"><a href="${path.siteRoot}/application/${application.id}">${application.name}</a></li>
                <li>Application ID : <span>${application.id}</span></li>
            </ul>
            <h2>Groups</h2>
            <div class="btn-group"><a href="#" class="btn blue">+ Add Group</a></div>
            <list:groups groups="${groups}" selected="${group.name}"/>
        </div>
        <div id="group-wrapper">
            <h2>Allowed Formats</h2>
            <div class="btn-group"><a href="${path.siteRoot}/application/${application.id}/group/${group.id}/format/create" class="btn green">+ Add Format</a></div>
            <list:formats formats="${formats}" selected="none"/>
            <c:choose>
                <c:when test="${images.numberOfImages > 0}">
                    <h2>( ${images.numberOfImages} ) ${images.numberOfImages == 1 ? 'image' : 'images'} found</h2>
                    <ul class="list images${images.numberOfImages > 12 ? ' small' :''}">
                        <c:forEach items="${images.list}" var="image">
                            <li><a href="${path.siteRoot}/image/${image.id}">${image.applicationId}/${image.groupId}/${image.path}/${image.id}.${image.extension}</a></li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <div class="btn-group"><a href="${path.siteRoot}/application/${application.id}/group/${group.id}/upload" class="btn red">+ Add Image</a></div>
                    <p class="empty">No images found.</p>
                </c:otherwise>
            </c:choose>
		</div>
	</t:body>
</t:page>