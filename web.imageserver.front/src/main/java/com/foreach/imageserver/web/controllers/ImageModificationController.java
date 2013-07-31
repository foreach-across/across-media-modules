package com.foreach.imageserver.web.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import com.foreach.imageserver.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.web.exceptions.ImageNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/modification")
public class ImageModificationController
{
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageService imageService;

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	@ResponseBody
	public String register( @RequestParam(value = "aid", required = true) int applicationId,
	                        @RequestParam(value = "token", required = true) String applicationKey,
	                        @RequestParam(value = "key", required = true) String imageKey,
	                        @RequestParam(value = "mod", required = true) ImageModifier modifier,
	                        Dimensions dimensions ) {
		Application application = applicationService.getApplicationById( applicationId );

		if ( application == null || !application.canBeManaged( applicationKey ) ) {
			throw new ApplicationDeniedException();
		}

		if ( dimensions == null || ( dimensions.getWidth() == 0 && dimensions.getHeight() == 0 ) ) {
			throw new ImageModificationException( "No width or heigh specified." );
		}

		Image image = imageService.getImageByKey( imageKey, application.getId() );

		if ( image == null ) {
			throw new ImageNotFoundException();
		}

		imageService.registerModification( image, dimensions, modifier );

		return StringUtils.EMPTY;
	}
}
