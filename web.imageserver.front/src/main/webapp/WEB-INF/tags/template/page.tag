<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/template" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<jsp:doBody varReader="templateRendering"/>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Imageserver - ${title}</title>
    <link href='http://fonts.googleapis.com/css?family=Droid+Sans:regular,bold&v1' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dijit/themes/tundra/tundra.css"/>
    <link rel="stylesheet" type="text/css" href="${path.resources}/css/main.css"/>
    <script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js" djConfig="parseOnLoad: true"></script>
    <script src="${path.resources}/js/is.js" ></script>
	${templateRenderingHead}
</head>
<body class="tundra">
<div id="container">
    <div id="breadcrumb">
		<ul>
			<li class="home active"><a href="${path.siteRoot}">ImageServer</a></li>
            <li class="arrow"><a href="${path.siteRoot}/application/list">Applications</a></li>
            <li class="arrow red"><a href="#">Application Example</a></li>
            <li class="arrow blue"><a href="#">Group Example</a></li>
            <li class="arrow green"><a href="#">Format Example</a></li>
		</ul>
        <div class="clearfix"><!-- styling empty div --></div>
	</div>

	<div id="content">
		${templateRenderingBody}
	</div>
</div>

</body>
</html>