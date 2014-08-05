package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController
{

	public static final String REGISTER_PATH = "register";
	public static final String UPDATE_RESOLUTION = "updateResolution";
	public static final String RESOLUTION_DETAILS = "resolutionDetails";
	public static final String LIST_RESOLUTIONS_PATH = "listResolutions";
	public static final String LIST_MODIFICATIONS = "listModifications";

	@Value("${accessToken}")
	private String accessToken;

	@Autowired
	private ContextService contextService;

	@Autowired
	private ImageService imageService;

	@RequestMapping(value = "/" + REGISTER_PATH, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public JsonResponse register( @RequestParam(value = "token", required = true) String accessToken,
	                              @RequestParam(value = "iid", required = true) String externalId,
	                              @RequestParam(value = "context", required = true) String contextCode,
	                              ImageModificationDto imageModificationDto ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Image image = imageService.getByExternalId( externalId );
		if ( image == null ) {
			return error( "No such image." );
		}

		Context context = contextService.getByCode( contextCode );
		if ( context == null ) {
			return error( "No such context." );
		}

		ImageResolution imageResolution =
				contextService.getImageResolution( context.getId(), imageModificationDto.getResolution().getWidth(),
				                                   imageModificationDto.getResolution().getHeight() );
		if ( imageResolution == null ) {
			return error(
					"No such image resolution : " + imageModificationDto.getResolution().getWidth() + "x" + imageModificationDto.getResolution().getHeight() );
		}

		CropGeneratorUtil.normalizeModificationDto( image, imageModificationDto );

		ImageModification modification = new ImageModification();
		modification.setImageId( image.getId() );
		modification.setContextId( context.getId() );
		modification.setResolutionId( imageResolution.getId() );
		modification.setCrop( DtoUtil.toBusiness( imageModificationDto.getCrop() ) );
		modification.setDensity( DtoUtil.toBusiness( imageModificationDto.getDensity() ) );

		try {
			imageService.saveImageModification( modification, image );
		}
		catch ( CropOutsideOfImageBoundsException e ) {
			return error( "Crop dimensions fall outside image bounds." );
		}

		return success();
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

	@RequestMapping(value = "/" + RESOLUTION_DETAILS, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse resolutionDetails( @RequestParam(value = "token", required = true) String accessToken,
	                                       @RequestParam(value = "id", required = true) int resolutionId ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ImageResolution resolution = imageService.getResolution( resolutionId );
		Collection<Context> contexts = contextService.getForResolution( resolutionId );

		List<String> contextNames = new ArrayList<>( contexts.size() );

		for ( Context ctx : contexts ) {
			contextNames.add( ctx.getCode() );
		}

		ImageResolutionFormDto dto = new ImageResolutionFormDto( DtoUtil.toDto( resolution ) );
		dto.setContext( contextNames.toArray( new String[contextNames.size()] ) );

		return success( dto );
	}

	@RequestMapping(value = "/" + UPDATE_RESOLUTION, method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse saveResolution( @RequestParam(value = "token", required = true) String accessToken,
	                                    @RequestBody ImageResolutionFormDto formDto ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ImageResolution resolution = DtoUtil.toBusiness( formDto.getResolution() );
		Collection<Context> contexts = new LinkedList<>();

		for ( String code : formDto.getContext() ) {
			contexts.add( contextService.getByCode( code ) );
		}

		imageService.saveImageResolution( resolution, contexts );

		return resolutionDetails( accessToken, resolution.getId() );
	}

	@RequestMapping(value = "/" + LIST_RESOLUTIONS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse listResolutions( @RequestParam(value = "token", required = true) String accessToken,
	                                     @RequestParam(value = "context", required = false) String contextCode,
	                                     @RequestParam(value = "configurableOnly", required = true,
	                                                   defaultValue = "false") boolean configurableOnly ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		List<ImageResolution> imageResolutions;

		if ( StringUtils.isNotBlank( contextCode ) ) {
			Context context = contextService.getByCode( contextCode );
			if ( context == null ) {
				return error( "No such context." );
			}

			imageResolutions = contextService.getImageResolutions( context.getId() );

		}
		else {
			imageResolutions = imageService.getAllResolutions();
		}

		if ( configurableOnly ) {
			removeNonConfigurableResolutions( imageResolutions );
		}

		return success( imageResolutionDtoList( imageResolutions ) );
	}

	private void removeNonConfigurableResolutions( List<ImageResolution> imageResolutions ) {
		Iterator<ImageResolution> iterator = imageResolutions.iterator();

		while ( iterator.hasNext() ) {
			ImageResolution resolution = iterator.next();

			if ( !resolution.isConfigurable() ) {
				iterator.remove();
			}
		}
	}

	@RequestMapping(value = "/" + LIST_MODIFICATIONS, method = RequestMethod.GET)
	@ResponseBody
	public JsonResponse listModifications( @RequestParam(value = "token", required = true) String accessToken,
	                                       @RequestParam(value = "iid", required = true) String externalId,
	                                       @RequestParam(value = "context", required = true) String contextCode ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Context context = contextService.getByCode( contextCode );
		if ( context == null ) {
			return error( "No such context." );
		}

		Image image = imageService.getByExternalId( externalId );
		if ( image == null ) {
			return error( String.format( "No image available for identifier %s.", externalId ) );
		}

		List<ImageModification> modifications = imageService.getModifications( image.getId(), context.getId() );

		return success( toModificationDtos( modifications ) );
	}

	private List<ImageModificationDto> toModificationDtos( List<ImageModification> modifications ) {
		List<ImageModificationDto> dtos = new ArrayList<>( modifications.size() );
		for ( ImageModification modification : modifications ) {
			dtos.add( toDto( modification ) );
		}
		return dtos;
	}

	private ImageModificationDto toDto( ImageModification modification ) {
		ImageResolution resolution = imageService.getResolution( modification.getResolutionId() );
		ImageModificationDto dto = new ImageModificationDto();
		dto.getResolution().setWidth( resolution.getWidth() );
		dto.getResolution().setHeight( resolution.getHeight() );
		dto.getCrop().setX( modification.getCrop().getX() );
		dto.getCrop().setY( modification.getCrop().getY() );
		dto.getCrop().setWidth( modification.getCrop().getWidth() );
		dto.getCrop().setHeight( modification.getCrop().getHeight() );
		dto.getDensity().setWidth( modification.getDensity().getWidth() );
		dto.getDensity().setHeight( modification.getDensity().getHeight() );
		return dto;
	}

	private List<ImageResolutionDto> imageResolutionDtoList( List<ImageResolution> imageResolutions ) {
		List<ImageResolutionDto> dtos = new ArrayList<>( imageResolutions.size() );
		for ( ImageResolution imageResolution : imageResolutions ) {
			ImageResolutionDto dto = DtoUtil.toDto( imageResolution );
			dtos.add( dto );
		}
		return dtos;
	}

}
