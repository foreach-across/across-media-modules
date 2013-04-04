<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
    <head>
        <META http-equiv="Content-Type" content="text/html;charset=UTF-8">
        <title>View Image</title>
    </head>
    <body>
        <h3>The image was successfully uploaded</h3>
        <form:form modelAttribute="model" method="get"><%-- enctype="multipart/form-data">--%>
            <fieldset>
                <legend>Uploaded image</legend>

                <c:choose>
                    <c:when test="${model.image.id > 0}">
                        Upload successfull.
                    </c:when>
                    <c:otherwise>
                        Upload ${status}
                    </c:otherwise>
                </c:choose>

                <p>
                    <form:hidden path="image.id"/>
                    Id: ${model.image.id}
                </p>

                <p>
                    <form:label for="image.applicationId" path="image.applicationId">Application id</form:label><br/>
                    <form:input path="image.applicationId"/>
                </p>

                <p>
                    <form:label for="image.groupId" path="image.groupId">Group Id</form:label><br/>
                    <form:input path="image.groupId"/>
                </p>

	            <p>
                    <a href="../imageUpload">Image Upload</a></li>
                </p><p>
	                <a href="imageList">List all images</a>
	            </p>

            </fieldset>
        </form:form>
    </body>
</html>