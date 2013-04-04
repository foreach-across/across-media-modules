<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<t:page title="Home">
	<t:body>
        <ul class="list">
            <li><a href="${path.siteRoot}/application/list">Applications</a></li>
            <li><a href="${path.siteRoot}/image/list">Images</a></li>
            <li><a href="${path.siteRoot}/log/list">Logs</a></li>
        </ul>
        <div class="btn-group"><a href="${path.siteRoot}/j_spring_security_logout" class="btn">Logout (<security:authentication property="principal.username"/>)</a></div>
	</t:body>
</t:page>