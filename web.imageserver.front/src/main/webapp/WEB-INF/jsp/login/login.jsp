<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<t:page title="Home">
    <t:body>
        <form name="f" action="<c:url value='/j_spring_security_check'/>" method="POST" class="form-login">
            <div class="form-item">
                <label for="username">User:</label>
                <input type='text' name='j_username' id="username" class="form-text"/>
            </div>
            <div class="form-item">
                <label for="password">Password:</label>
                <input type='password' name='j_password' id="password" class="form-text"/>
            </div>
            <div class="form-inline-item">
                <input type='checkbox' id="remember_me" name='_spring_security_remember_me'  class="form-checkbox"/>
                <label for="remember_me">Remember me</label>
            </div>
            <div class="form-item">
                <input name="submit" type="submit" value="Login" class="btn">
            </div>
        </form>
    </t:body>
</t:page>