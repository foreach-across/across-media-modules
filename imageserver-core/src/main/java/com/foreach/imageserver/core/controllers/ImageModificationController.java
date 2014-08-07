package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.CropGeneratorUtil;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.CropOutsideOfImageBoundsException;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@ImageServerController
@ResponseBody
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController
{
	public static final String REGISTER_PATH = "register";
	public static final String LIST_MODIFICATIONS = "listModifications";

	@Autowired
	private ImageContextService contextService;

	@Autowired
	private ImageService imageService;

	public ImageModificationController( String accessToken ) {
		super( accessToken );
	}

	@RequestMapping(value = "/" + REGISTER_PATH, method = { RequestMethod.GET, RequestMethod.POST })
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

		ImageContext context = contextService.getByCode( contextCode );
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

	@RequestMapping(value = "/" + LIST_MODIFICATIONS, method = RequestMethod.GET)
	public JsonResponse listModifications( @RequestParam(value = "token", required = true) String accessToken,
	                                       @RequestParam(value = "iid", required = true) String externalId,
	                                       @RequestParam(value = "context", required = true) String contextCode ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ImageContext context = contextService.getByCode( contextCode );
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
}
