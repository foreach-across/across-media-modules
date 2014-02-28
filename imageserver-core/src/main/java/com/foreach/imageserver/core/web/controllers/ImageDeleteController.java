package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/delete")
public class ImageDeleteController
{
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageService imageService;

	@RequestMapping("/all")
	@ResponseBody
	public String delete( @RequestParam(value = "aid", required = true) int applicationId,
	                      @RequestParam(value = "token", required = true) String applicationKey,
	                      @RequestParam(value = "key", required = true) String imageKey ) {
		return delete( applicationId, applicationKey, imageKey, false );
	}

	@RequestMapping("/variants")
	@ResponseBody
	public String deleteVariants( @RequestParam(value = "aid", required = true) int applicationId,
	                              @RequestParam(value = "token", required = true) String applicationKey,
	                              @RequestParam(value = "key", required = true) String imageKey ) {
		return delete( applicationId, applicationKey, imageKey, true );
	}

	private String delete( int applicationId, String applicationKey, String imageKey, boolean variantsOnly ) {
		Application application = applicationService.getApplicationById( applicationId );

		if ( application == null || !application.canBeManaged( applicationKey ) ) {
			throw new ApplicationDeniedException();
		}

		Image image = imageService.getImageByKey( imageKey, application.getId() );

		if ( image != null ) {
			imageService.delete( image, variantsOnly );
		}

		return StringUtils.EMPTY;
	}
}
