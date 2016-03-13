package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;

/**
 * @author Arne Vandamme
 * @since 3.5
 */
@ImageServerController
public class ImageDeleteController extends BaseImageAPIController
{
	public static final String DELETE_IMAGE_PATH = "/api/image/delete";

	@Autowired
	private ImageService imageService;

	public ImageDeleteController( String accessToken ) {
		super( accessToken );
	}

	@RequestMapping(value = DELETE_IMAGE_PATH, method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse deleteImage( @RequestParam(value = "token", required = true) String accessToken,
	                                 @RequestParam(value = "iid", required = true) String externalId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}
		boolean deleted = imageService.deleteImage( externalId );

		return success( Collections.singletonMap( "deleted", deleted ) );
	}
}
