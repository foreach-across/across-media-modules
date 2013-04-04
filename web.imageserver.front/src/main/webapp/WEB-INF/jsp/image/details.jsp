<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<t:page title="image">
	<t:body>
		<a href="${path.siteRoot}/${model.originalPath}">
			<img id="imagePreview" src="${path.siteRoot}/${model.imagePath}" alt="${model.image.originalFileName}"/>
		</a>
		<h2>image ID: <span>${model.image.id}</span></h2>
        <ul class="meta">
            <li>File name: <span>${model.image.originalFileName}</span></li>
            <li>Application: <span class="grayedId"><a href="${path.siteRoot}/application/${model.application.id}">${model.application.name} (${model.application.id})</a></span></li>
            <li>Group: <span class="grayedId"><a href="${path.siteRoot}/application/${model.application.id}/group/${model.group.id}">${model.group.name} (${model.group.id})</a></span></li>
            <li>Dimensions: <span>${model.image.width} x ${model.image.height}</span></li>
            <li>File size: <span>${model.image.fileSize}</span></li>
            <li>File path: <span>${model.image.path}</span></li>
            <li>Date created: <span><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${model.image.dateCreated}"/></span></li>
            <c:if test="${model.image.deleted}">
                <li class="grayedId">Image deleted</li>
            </c:if>
        </ul>

        <h3>Existing crops:</h3>
         <c:choose>
            <c:when test="${model.cropListSize > 0}">
                <c:forEach items="${model.image.crops}" var="crop">
                    <p><a href="${path.siteRoot}/image/${model.image.id}/crop/create">${crop}</a></p>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p>No crops defined.</p>
            </c:otherwise>
        </c:choose>

        <div class="btn-group"><a href="${path.siteRoot}/image/${model.image.id}/crop/create" title="Create Crop" class="btn">Edit crops</a></div>

	</t:body>
</t:page>