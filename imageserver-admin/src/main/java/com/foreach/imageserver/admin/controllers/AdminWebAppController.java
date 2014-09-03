package com.foreach.imageserver.admin.controllers;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.template.ClearTemplate;
import net.engio.mbassy.listener.Handler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@AdminWebController
public class AdminWebAppController
{
	private final String accessToken;
	private final String imageServerUrl;

	public AdminWebAppController( String imageServerUrl, String accessToken ) {
		this.accessToken = accessToken;
		this.imageServerUrl = imageServerUrl;
	}

	@Handler
	public void registerMenu( AdminMenuEvent adminMenuEvent ) {
		adminMenuEvent.builder().item( "/image-server", "Image server" );
	}

	@ModelAttribute
	public void init( WebResourceRegistry resourceRegistry ) {
		resourceRegistry.addWithKey( WebResource.CSS, "ImageServerAdminWeb", "/css/imageserver-admin/admin.css",
		                             WebResource.VIEWS );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "angular",
		                             "https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0-beta.8/angular.js",
		                             WebResource.EXTERNAL );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "angular-route",
		                             "https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0-beta.8/angular-route.js",
		                             WebResource.EXTERNAL );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "angular-file-upload",
		                             "/js/imageserver-admin/angular-file-upload.js", WebResource.VIEWS );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "angular-ui-utils",
		                             "/js/imageserver-admin/ui-utils-0.1.1/ui-utils.min.js", WebResource.VIEWS );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "ImageServerAdminWeb",
		                             "/js/imageserver-admin/admin-app.js", WebResource.VIEWS );
	}

	@RequestMapping("/image-server")
	public String bootstrapWebApp( Model model ) {
		model.addAttribute( "accessToken", accessToken );
		model.addAttribute( "imageServerUrl", imageServerUrl );

		return "th/imageserver-admin/admin-app";
	}

	@RequestMapping("/partial/{view}")
	@ClearTemplate
	public String angularView( @PathVariable("view") String path ) {
		return "th/imageserver-admin/partial/" + path;
	}

	@RequestMapping("/upload")
	public String showUpload() {
		return "th/imageserver-admin/upload";
	}

	@RequestMapping("/view")
	public String seeImage() {
		return "th/imageserver-admin/view";
	}
}
