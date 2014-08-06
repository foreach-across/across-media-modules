package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.DtoUtil;
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
	public static final String LOAD_IMAGE_PATH = "load";
	public static final String IMAGE_EXISTS_PATH = "imageExists";
	public static final String IMAGE_INFO_PATH = "imageInfo";
	public static final String CONTEXT_LIST = "/context/list";

	@Autowired
	private ImageService imageService;

	@Autowired
	private ContextService contextService;

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

		Collection<Context> contexts = contextService.getAllContexts();
		List<String> contextNames = new ArrayList<>( contexts.size() );

		for ( Context ctx : contexts ) {
			contextNames.add( ctx.getCode() );
		}

		return success( contextNames );
	}

	@RequestMapping(value = "/" + LOAD_IMAGE_PATH, method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse load( @RequestParam(value = "token", required = true) String accessToken,
	                          @RequestParam(value = "iid", required = true) String externalId,
	                          @RequestParam(value = "imageData", required = true) byte[] imageData,
	                          @RequestParam(value = "imageTimestamp", required = false) Long imageTimestamp ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Date imageDate = ( imageTimestamp != null ) ? new Date( imageTimestamp ) : new Date();
		Image image = imageService.saveImage( externalId, imageData, imageDate );

		return success( DtoUtil.toDto( image ) );
	}

	@RequestMapping(value = "/" + IMAGE_EXISTS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse exists( @RequestParam(value = "token", required = true) String accessToken,
	                            @RequestParam(value = "iid", required = true) String externalId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Image image = imageService.getByExternalId( externalId );

		return success( image != null );
	}

	@RequestMapping(value = "/" + IMAGE_INFO_PATH, method = RequestMethod.GET)
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
}

