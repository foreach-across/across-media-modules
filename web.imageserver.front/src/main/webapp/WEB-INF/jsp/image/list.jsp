<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<t:page title="Images">
	<t:body>
		<h2>Images</h2>
		<div class="btn-group"><a href="${path.siteRoot}/image/upload" class="btn">Upload new image</a></div>
		<ul class="list images">
			<c:forEach items="${images}" var="image">
				<li><a href="${path.siteRoot}/image/${image.id}">${image.applicationId}/${image.groupId}/${image.path}/${image.id}.${image.extension}</a></li>
			</c:forEach>
		</ul>
	</t:body>
</t:page>