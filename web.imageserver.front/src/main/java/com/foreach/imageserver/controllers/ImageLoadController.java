package com.foreach.imageserver.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.controllers.exception.ApplicationDeniedException;
import com.foreach.imageserver.controllers.exception.ImageForbiddenException;
import com.foreach.imageserver.controllers.exception.ImageLookupException;
import com.foreach.imageserver.controllers.exception.ImageNotFoundException;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ImageLoadController
{
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageLookupRepository imageLookupRepository;

	@Autowired
	private ImageService imageService;

	@RequestMapping("/load")
	@ResponseBody
	public String load( @RequestParam(value = "aid", required = true) int applicationId,
	                    @RequestParam(value = "token", required = true) String applicationKey,
	                    @RequestParam(value = "uri", required = true) String repositoryURI,
	                    @RequestParam(value = "key", required = false) String targetKey ) {
		Application application = applicationService.getApplicationById( applicationId );

		if ( application == null || !application.canBeManaged( applicationKey ) ) {
			throw new ApplicationDeniedException();
		}

		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( repositoryURI );
		ensureLookupResultIsValid( lookupResult );

		String imageKey = StringUtils.defaultIfEmpty( targetKey, repositoryURI );
		Image image = imageService.getImageByKey( imageKey, application.getId() );

		if ( image == null ) {
			image = createNewImage( application, imageKey );
		}

		imageService.save( image, lookupResult );

		return StringUtils.EMPTY;
	}

	private Image createNewImage( Application application, String imageKey ) {
		Image image = new Image();
		image.setApplicationId( application.getId() );
		image.setKey( imageKey );

		return image;
	}

	private void ensureLookupResultIsValid( RepositoryLookupResult result ) {
		if ( result == null || result.getStatus() == null ) {
			throw new ImageLookupException();
		}

		switch ( result.getStatus() ) {
			case ERROR:
				throw new ImageLookupException();
			case NOT_FOUND:
				throw new ImageNotFoundException();
			case ACCESS_DENIED:
				throw new ImageForbiddenException();
		}
	}

}
