<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<t:page title="format">
	<t:body>
       <h3>Format</h3>

        <c:if test="${isSavedOk}">
            <p>format has been saved successfully</p>
        </c:if>

        <form:errors path="*" element="div" cssClass="feedback error" />

		<form:form modelAttribute="formatUploadModel" method="post">
			<div class="form-item">
				<form:label for="name" path="name">Name</form:label>
				<form:input path="name" />
			</div>
			<div class="form-item">
				<form:label for="width" path="width">width</form:label>
				<form:input path="width" />
			</div>
			<div class="form-item">
				<form:label for="height" path="height">height</form:label>
				<form:input path="height" />
			</div>
			<div class="form-item">
				<form:label for="absolute" path="absolute">absolute</form:label>
				<form:checkbox path="absolute" />
			</div>
			<div class="form-item">
				<input type="submit" value="save" name="save" class="btn"/>
                <input type="submit" value="delete" name="delete" class="btn"/>
                <div class="clearfix"><!-- styling empty div --></div>
                <a href="${path.siteRoot}/application/${applicationId}/group/${groupId}">cancel</a>
			</div>
		</form:form>

	</t:body>
</t:page>