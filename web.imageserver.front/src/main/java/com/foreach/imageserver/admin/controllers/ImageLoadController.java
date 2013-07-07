package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Controller
public final class ImageLoadController
{
	//private static final Logger LOG = LoggerFactory.getLogger( ImageLoadController.class );

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageLookupRepository imageLookupRepository;

	@Autowired
	private ImageService imageService;

	@RequestMapping("/load")
	public void load( int applicationId, String applicationKey, String repositoryURI, String targetKey ) {
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

	@ResponseStatus(HttpStatus.FORBIDDEN)
	public static class ApplicationDeniedException extends RuntimeException
	{
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class ImageNotFoundException extends RuntimeException
	{
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	public static class ImageForbiddenException extends RuntimeException
	{
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public static class ImageLookupException extends RuntimeException
	{
	}
}
