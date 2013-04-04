<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<t:page title="Upload image">
	<t:body>
		<p>Image preview for ${imageId}</p>
		<img src="${imagePath}"/>

		<p><a href="${fullImagePath}">Full image link</a></p>

		<div class="btn-group">
			<a href="${cropUrl}" title="Create Crop" class="btn">Edit crops</a>
			<a href="${cropUrl2}" title="Create Crop" class="btn">Edit crops for version 2</a>
		</div>
	</t:body>
</t:page>