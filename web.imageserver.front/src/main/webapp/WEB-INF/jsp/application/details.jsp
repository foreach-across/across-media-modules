<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="list" tagdir="/WEB-INF/tags/list" %>
<t:page title="${application.name}">
	<t:body>
        <div id="app-wrapper">
            <ul class="meta">
                <li class="title">${application.name}</li>
                <li>Application ID : <span>${application.id}</span></li>
            </ul>
            <h2>Groups</h2>
            <div class="btn-group"><a href="#" class="btn blue">+ Add Group</a></div>
            <list:groups groups="${groups}" selected="none"/>
        </div>
	</t:body>
</t:page>