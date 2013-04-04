<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<t:page title="Images">
	<t:body>
		<h2>Logs: </h2>
        <h3>All requests made in the last hour.<h3>
		<ul class="list logs">
			<c:forEach items="${logs.list}" var="log">
				<li>
                    <p>${log.description}</p>
                </li>
			</c:forEach>
		</ul>
	</t:body>
</t:page>