<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<t:page title="Applications">
	<t:body>
        <h2><span class="red-text">${numberOfApplications}</span> applications found</h2>
        <div class="btn-group"><a href="#" class="btn red">+ Add Application</a></div>
        <table>
            <tr class="head">
                <th>Application</th>
                <th>Groups</th>
                <th>Images</th>
                <th><!-- empty header --></th>
            </tr>
            <c:forEach items="${applications}" var="application">
                <tr>
                    <td class="title"><a href="${path.siteRoot}/application/${application.id}">${application.name}</a></td>
                    <td>${application.numberOfGroups}</td>
                    <td>${application.numberOfImages}</td>
                    <td class="action">
                        <span>
                            <a href="${path.siteRoot}/application/${application.id}">view</a> | <a href="#">edit</a>
                        </span>
                    </td>
                </tr>
			</c:forEach>
        </table>
	</t:body>
</t:page>