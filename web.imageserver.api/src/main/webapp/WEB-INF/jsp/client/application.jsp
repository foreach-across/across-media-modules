<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<t:page title="Upload image">
	<t:body>
		<h3>Image preview</h3>
		<img src="${imagePath}"/>

		<h3>Upload image</h3>
        <c:choose>
            <c:when test="${status.failure != null && not status.failure}">
                <p class="success">Image with has been uploaded succesfuly.</p>
                <p class="success">Imageid: ${model.imageId}</p>
                <div class="btn-group"><a href="${cropUrl}" title="Create Crop" class="btn">Edit crops</a></div>
            </c:when>
            <c:when test="${status.failure}"><p class="error">Error occured: ${status.description}</p></c:when>
        </c:choose>

		<form:form modelAttribute="model" method="post" enctype="multipart/form-data">
            <div class="form-item">
				<form:label for="applicationId" path="applicationId">Application id</form:label>
				<form:input path="applicationId"/>
			</div>
			<div class="form-item">
				<form:label for="groupId" path="groupId">Group</form:label>
				<form:input path="groupId"/>
			</div>
			<div class="form-item">
				<form:label for="imageId" path="imageId">ImageId</form:label>
				<form:input path="imageId"/>
			</div>
			<div class="form-item">
				<form:label for="imageData" path="imageData">Image</form:label>
				<form:input path="imageData" type="file"/>
			</div>
			<div class="form-item">
				<input type="submit" value="upload" class="btn"/>
                <div class="clearfix"><!-- styling empty div --></div>
			</div>
		</form:form>
	</t:body>
</t:page>