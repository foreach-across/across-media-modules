<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" type="text/css" href="${path.resources}/css/cropper.css"/>
    <link rel="stylesheet" type="text/css" href="${path.resources}/css/style.css"/>

    <script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js" djConfig="parseOnLoad: true"></script>

    <script type="text/javascript" src="${path.resources}/js/Cropper/Toolkit.js"></script>
    <script type="text/javascript" src="${path.resources}/js/Cropper/Cropper.js"></script>
    <script type="text/javascript" src="${path.resources}/js/cropform.js"></script>

    <script type="text/javascript">
        dojo.require("dijit.dijit");
        dojo.require("dojox.layout.ContentPane");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.AccordionContainer");
        dojo.require("dojo.parser");
        dojo.require("dojo.html");
        dojo.require("dojo.dnd.Source");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.Menu");
        dojo.require("dojo.NodeList-fx");
        dojo.require("dijit.Tooltip");
        dojo.require("dojo.io.iframe");
        dojo.require("dojo.cookie");
        dojo.require("dojox.encoding.base64");
        dojo.require("dojo.back");
    </script>
</head>
<body>

<c:set var="baseUrl">${path.siteRoot}${baseActionUrl}</c:set>

<c:if test="${versionSwitch}">
	<form action="#" method="get">
		<select name="version" onChange="javascript:document.location='${baseUrl}/${aspectRatio.stringForUrl}/${targetWidth}/' + this.value +'/true' ">>
			<c:forEach items="${versions}" var="versionValue">
				<c:choose>
					<c:when test="${versionValue==version}">
						<option value="${versionValue}" selected="selected">${versionValue}</option>
					</c:when>
					<c:otherwise>
						<option value="${versionValue}">${versionValue}</option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</select>
	</form>
</c:if>

<form action="${path.siteRoot}${fullActionUrl}" method="post" id="cropForm">

	<input name="left" id="cropX" type="hidden" value="${rect.left}"/>
	<input name="top" id="cropY" type="hidden" value="${rect.top}"/>
	<input name="width" id="cropW" type="hidden" value="${rect.width}"/>
	<input name="height" id="cropH" type="hidden" value="${rect.height}"/>

    <input name="imageId" id="imageId" type="hidden" value="${crop.imageId}"/>
    <input name="cropId" id="cropId" type="hidden" value="${crop.id}"/>
    <%-- <input name="formatId" id="formatId" type="hidden" value="${crop.formatId}"/> --%>

    <input name="targetWidth" type="hidden" value="${selectedFormat.width}" />
    <input name="cropAspectRatio" type="hidden" value="${aspectRatio}" />
	<input name="fixedVersion" type="hidden" value="${!versionSwitch}" />

	<input name="imageW" id="imageW" onchange="Cropper.formResetImgW();" type="hidden">
    <input name="imageH" id="imageH" onchange="Cropper.formResetImgH();" type="hidden">


    <div class="container-tool">

        <div class="wrap-image">

            <div class="img-tools">
                <a href="#" onclick="Cropper.selectMaxAspectRatio(); return false;" class="tool-max">select max</a>
                <a href="#" onclick="Cropper.cropToAspectRatio(); return false;" class="tool-crop">crop</a>
                <a href="#" onclick="Cropper.extendToAspectRatio(); return false;" class="tool-extend">extend</a>
                <a href="#" onclick="Cropper.reset(); return false;" class="tool-reset">reset</a>
            </div>
            <div id="divCropImageBorder" style="width: ${displaySize.width}px">

                <div id="cropImageContainer">
                    <img src="${path.siteRoot}/${imageUrl}/cfalse" width="${displaySize.width}" height="${displaySize.height}"
                         id="cropImage"  style="opacity:0.4;filter:alpha(opacity=40)" alt="image to crop"/>
                    <img src="${path.siteRoot}/${imageUrl}/cfalse" width="${displaySize.width}" height="${displaySize.height}"
                         id="imageCropSelection">
                    <div style="width: ${displaySize.width}; height: ${displaySize.height}; visibility: hidden;" id="divShield"></div>
                </div>

                <input type="submit" onclick="return cropFormObj.doCrop();" class="CF normal_btn popup" style="float:right;margin-top:10px;" value="save"/>

            </div>
        </div>

    </div>

    <div class="container-sizes">
        <h2>Aspect ratio ${aspectRatio} (${aspectRatioIndex + 1} of ${aspectRatioCount})</h2>
        <ul>
            <li class="first" ${formats.selected eq formats.generic ? 'id="active"' : ''}>
                <a href="${baseUrl}/${aspectRatio.stringForUrl}/0/${version}/${versionSwitch}" class="popup"> all formats
                    <span id="cropOkSpan_all">&nbsp;</span>
                </a>
            </li>
            <c:forEach items="${formats.specificFormats}" var="format" varStatus="i">
                <li ${formats.selected eq format ? 'id="active"' : ''}${classString}>
	                <a href="${baseUrl}/${aspectRatio.stringForUrl}/${format.width}/${version}/${versionSwitch}" class="popup">${format} [${format.width}]</a>
                </li>
            </c:forEach>
        </ul>

        <c:if test="${ prevRatio != null }">
            <div class="buttongroup left">
                    <a href="${baseUrl}/${prevRatio.stringForUrl}/0/${version}/${versionSwitch}" class="CF normal_btn popup" >previous</a>
            </div>
        </c:if>
        <c:choose>
            <c:when test="${nextRatio != null }">
                 <div class="buttongroup">
                        <a href="${baseUrl}/${nextRatio.stringForUrl}/0/${version}/${versionSwitch}" class="CF normal_btn popup" >next</a>
                 </div>
            </c:when>
            <c:otherwise>
                <div class="buttongroup">
                        <a href="#" class="CF normal_btn" onclick="cropFormObj.doClose();" >finish</a>
                 </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="clearfix">
	</div>

</form>

<c:set var="selectedFormatId">${selectedFormat != null ? selectedFormat.id : "all"}</c:set>

<script type="text/javascript">
var cropFormObj;

dojo.addOnLoad( function()
{

    cropFormObj = new CropForm(
        '${aspectRatio.lossyRepresentation}', // aspectratio factor
        ${minWidth}, // minimum width needed
        '',
        '${baseUrl}',
        '${numAspectRatio}', //  aspect ratio count
        '${path.siteRoot}',
        ${crop.imageId},  // id
        '${selectedFormatId}',
        false,
        ${displaySize.width}, // preview width == actual width
        ${image.width}, // actual width
        ${image.height}, // actual height
        '<c:forEach items="${formats.specificFormats}" var="format" varStatus="i">${!i.first?',':''}${format.id}</c:forEach>',
        ${rect.left},
        ${rect.top},
        ${rect.width},
        ${rect.height}
    )
}
);

</script>

</body>
</html>
