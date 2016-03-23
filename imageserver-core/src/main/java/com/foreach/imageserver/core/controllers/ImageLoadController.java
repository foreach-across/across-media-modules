package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.rest.response.PregenerateResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageInfoDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ImageServerController
public class ImageLoadController extends BaseImageAPIController
{
	public static final String LOAD_IMAGE_PATH = "/api/image/load";
	public static final String IMAGE_INFO_PATH = "/api/image/details";
	public static final String CONTEXT_LIST = "/api/context/list";
	public static final String IMAGE_PREGENERATE = "/api/image/pregenerate";

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageRestService imageRestService;

	@Autowired
	private ImageContextService contextService;

	public ImageLoadController( String accessToken ) {
		super( accessToken );
	}

	@InitBinder
	public void initBinder( ServletRequestDataBinder binder ) {
		binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor() );
	}

	@RequestMapping(value = CONTEXT_LIST, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse listContexts( @RequestParam(value = "token", required = true) String accessToken ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Collection<ImageContext> contexts = contextService.getAllContexts();
		List<String> contextNames = new ArrayList<>( contexts.size() );

		for ( ImageContext ctx : contexts ) {
			contextNames.add( ctx.getCode() );
		}

		return success( contextNames );
	}

	@RequestMapping(value = LOAD_IMAGE_PATH, method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse load( @RequestParam(value = "token", required = true) String accessToken,
	                          @RequestParam(value = "iid", required = true) String externalId,
	                          @RequestParam(value = "imageData", required = true) byte[] imageData,
	                          @RequestParam(value = "imageTimestamp", required = false) Long imageTimestamp,
	                          @RequestParam(value = "replaceExisting", required = false, defaultValue = "false") boolean replaceExisting ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		try {
			Date imageDate = ( imageTimestamp != null ) ? new Date( imageTimestamp ) : new Date();
			Image image = imageService.saveImage( externalId, imageData, imageDate, replaceExisting );

			return success( DtoUtil.toDto( image ) );
		}
		catch ( RuntimeException ise ) {
			return error( ise.getMessage() );
		}
	}

	@RequestMapping(value = IMAGE_INFO_PATH, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse info( @RequestParam(value = "token", required = true) String accessToken,
	                          @RequestParam(value = "iid", required = true) String externalId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Image image = imageService.getByExternalId( externalId );

		if ( image != null ) {
			return success( DtoUtil.toDto( image ) );
		}
		else {
			ImageInfoDto notExisting = new ImageInfoDto();
			notExisting.setExternalId( externalId );
			notExisting.setExisting( false );

			return success( notExisting );
		}
	}

	@RequestMapping(value = IMAGE_PREGENERATE, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse pregenerate( @RequestParam(value = "token", required = true) String accessToken,
	                                 @RequestParam(value = "iid", required = true) String externalId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		PregenerateResolutionsResponse response = imageRestService.pregenerateResolutions( externalId );

		if ( response.isImageDoesNotExist() ) {
			return error( "Image does not exist." );
		}

		return success( response.getImageResolutions() );
	}
}

