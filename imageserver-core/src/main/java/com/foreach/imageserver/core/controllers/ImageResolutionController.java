package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ResolutionRestService;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Arne Vandamme
 */
@ImageServerController
@ResponseBody
@RequestMapping("/modification")
public class ImageResolutionController extends BaseImageAPIController
{
	public static final String UPDATE_RESOLUTION = "updateResolution";
	public static final String RESOLUTION_DETAILS = "resolutionDetails";
	public static final String LIST_RESOLUTIONS_PATH = "listResolutions";

	@Autowired
	private ImageContextService contextService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ResolutionRestService resolutionRestService;

	public ImageResolutionController( String accessToken ) {
		super( accessToken );
	}

	@RequestMapping(value = "/" + RESOLUTION_DETAILS, method = RequestMethod.GET)
	public JsonResponse resolutionDetails( @RequestParam(value = "token", required = true) String accessToken,
	                                       @RequestParam(value = "id", required = true) long resolutionId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ImageResolution resolution = imageService.getResolution( resolutionId );

		List<String> contextNames = new ArrayList<>( resolution.getContexts().size() );

		for ( ImageContext ctx : resolution.getContexts() ) {
			contextNames.add( ctx.getCode() );
		}

		ImageResolutionFormDto dto = new ImageResolutionFormDto( DtoUtil.toDto( resolution ) );
		dto.setContext( contextNames.toArray( new String[contextNames.size()] ) );

		return success( dto );
	}

	@RequestMapping(value = "/" + UPDATE_RESOLUTION, method = RequestMethod.POST)
	public JsonResponse saveResolution( @RequestParam(value = "token", required = true) String accessToken,
	                                    @RequestBody ImageResolutionFormDto formDto ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ImageResolution resolution = DtoUtil.toBusiness( formDto.getResolution() );
		Collection<ImageContext> contexts = new LinkedList<>();

		for ( String code : formDto.getContext() ) {
			contexts.add( contextService.getByCode( code ) );
		}

		imageService.saveImageResolution( resolution, contexts );

		return resolutionDetails( accessToken, resolution.getId() );
	}

	@RequestMapping(value = "/" + LIST_RESOLUTIONS_PATH, method = RequestMethod.GET)
	public JsonResponse listResolutions( @RequestParam(value = "token", required = true) String accessToken,
	                                     ListResolutionsRequest listResolutionsRequest ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ListResolutionsResponse response = resolutionRestService.listResolutions( listResolutionsRequest );

		if ( response.isContextDoesNotExist() ) {
			return error( "No such context." );
		}

		return success( response.getImageResolutions() );
	}

	static class ImageResolutionFormDto
	{
		private ImageResolutionDto resolution;
		private String[] context = new String[0];

		ImageResolutionFormDto() {
		}

		ImageResolutionFormDto( ImageResolutionDto resolution ) {
			this.resolution = resolution;
		}

		public ImageResolutionDto getResolution() {
			return resolution;
		}

		public void setResolution( ImageResolutionDto resolution ) {
			this.resolution = resolution;
		}

		public String[] getContext() {
			return context;
		}

		public void setContext( String[] context ) {
			this.context = context;
		}
	}

}
