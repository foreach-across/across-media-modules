package com.foreach.imageserver.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.controllers.exception.ImageLookupException;
import com.foreach.imageserver.controllers.exception.ImageNotFoundException;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class ImageStreamingController
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageStreamingController.class );

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageService imageService;

	@RequestMapping("/view")
	public void view( @RequestParam(value = "aid", required = true) int applicationId,
	                  @RequestParam(value = "key", required = true) String imageKey,
	                  HttpServletResponse response ) {

		Application application = applicationService.getApplicationById( applicationId );

		if ( application == null || !application.isActive() ) {
			LOG.debug( "Application not found or inactive {}", applicationId );
			throw new ImageNotFoundException();
		}

		Image image = imageService.getImageByKey( imageKey, application.getId() );

		if ( image == null ) {
			throw new ImageNotFoundException();
		}

		ImageFile imageFile = imageService.fetchImageFile( image );

		response.setStatus( HttpStatus.OK.value() );
		response.setContentType( imageFile.getImageType().getContentType() );
		response.setContentLength( Long.valueOf( imageFile.getFileSize() ).intValue() );

		InputStream content = imageFile.getContent();

		try {
			IOUtils.copy( content, response.getOutputStream() );
		}
		catch ( IOException ioe ) {
			throw new ImageLookupException( ioe );
		}
		finally {
			IOUtils.closeQuietly( content );
		}
	}
}
