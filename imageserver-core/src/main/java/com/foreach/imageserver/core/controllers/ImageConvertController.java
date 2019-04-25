package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageConvertDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import java.io.IOException;

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

	@PostMapping
	@ResponseBody
	public JsonResponse convertImage( @RequestParam(value = "token", required = true) String accessToken,
	                                  @RequestBody ImageConvertDto convertDto ) throws IOException {

		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		return success( imageService.convertImageToTargets( convertDto ) );
	}
}
