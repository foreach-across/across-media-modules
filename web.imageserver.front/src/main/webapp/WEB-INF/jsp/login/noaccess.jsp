<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<t:page title="Home">
    <t:body>
    <h1><c:out value="Admin Home Page"/></h1><br>
    User '<security:authentication property="principal.name"/>' has no access granted to Admin application<br>
	<a href="<c:url value="/j_spring_security_logout"/>">Logout</a>
    </t:body>
</t:page>