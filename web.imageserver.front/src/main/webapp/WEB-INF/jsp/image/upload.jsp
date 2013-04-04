<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%-- TODO cleanup class="${uploadStatus.status}" after refactor --%>
<p>${uploadStatus.description}</p>
<form:form cssClass="form-block" modelAttribute="model" method="post" action="${path.siteRoot}/application/${applicationId}/group/${groupId}/upload" enctype="multipart/form-data">
    <div class="form-item">
        <form:label for="image" path="image">Image</form:label>
        <form:input path="image" type="file"/>
    </div>
    <div class="form-item">
        <input type="submit" value="upload" class="btn"/>
         <div class="clearfix"><!-- styling empty div --></div>
    </div>
</form:form>