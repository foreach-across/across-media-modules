package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageConvertDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

/**
 * Controller that converts a supplied image using the given transformations that returns the generated image in real-time
 *
 * @author Wouter Van Hecke
 * @since 5.0.0
 */
@ImageServerController
@RequestMapping(ImageConvertController.CONVERT_IMAGE_PATH)
public class ImageConvertController extends BaseImageAPIController
{
	public static final String CONVERT_IMAGE_PATH = "/api/image/convert";

	@Autowired
	private ImageService imageService;

	public ImageConvertController( String accessToken ) {
		super( accessToken );
	}

	@InitBinder
	public void initBinder( ServletRequestDataBinder binder ) {
		binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor() );
	}

	/**
	 * Converts the supplied image using the given transformations and returns the results in real-time
	 *
	 * @param accessToken access token
	 * @param convertDto  the source image and a list of targets + transformations
	 * @return the generated images
	 */
	@PostMapping
	@ResponseBody
	public JsonResponse convertImage( @RequestParam(value = "token", required = true) String accessToken,
	                                  @RequestBody ImageConvertDto convertDto ) {

		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		return success( imageService.convertImageToTargets( convertDto ) );
	}
}
