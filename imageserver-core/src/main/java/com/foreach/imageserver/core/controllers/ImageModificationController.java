package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.response.ListModificationsResponse;
import com.foreach.imageserver.core.rest.response.RegisterModificationResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@ImageServerController
@ResponseBody
@RequestMapping("/api/modification")
public class ImageModificationController extends BaseImageAPIController
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageModificationController.class );

	public static final String REGISTER_PATH = "/register";
	public static final String REGISTER_LIST_PATH = "/registerlist";
	public static final String LIST_MODIFICATIONS = "/list";

	@Autowired
	private ImageRestService imageRestService;

	public ImageModificationController( String accessToken ) {
		super( accessToken );
	}

	@RequestMapping(value = REGISTER_PATH, method = { RequestMethod.GET, RequestMethod.POST })
	public JsonResponse register( @RequestParam(value = "token", required = true) String accessToken,
	                              @RequestParam(value = "iid", required = true) String externalId,
	                              @RequestParam(value = "context", required = true) String contextCode,
	                              ImageModificationDto imageModificationDto ) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		RegisterModificationRequest request = new RegisterModificationRequest();
		request.setExternalId( externalId );
		request.setContext( contextCode );
		request.setImageModificationDto( imageModificationDto );

		RegisterModificationResponse response = imageRestService.registerModifications( request );

		if ( response.isContextDoesNotExist() ) {
			return error( "No such context." );
		}

		if ( response.isImageDoesNotExist() ) {
			return error( String.format( "No image available for identifier %s.", request.getExternalId() ) );
		}

		if ( response.isResolutionDoesNotExist() ) {
			return error(
					"No such image resolution : " + imageModificationDto.getResolution().getWidth() + "x" + imageModificationDto.getResolution().getHeight() );
		}

		if ( response.isCropOutsideOfImageBounds() ) {
			return error( "Crop dimensions fall outside image bounds." );
		}

		return success();
	}

	@RequestMapping(value = REGISTER_LIST_PATH, method = { RequestMethod.GET, RequestMethod.POST })
	public JsonResponse register( @RequestParam(value = "token", required = true) String accessToken,
	                              @RequestParam(value = "iid", required = true) String externalId,
	                              @RequestParam(value = "context", required = true) String contextCode,
	                              @RequestParam LinkedMultiValueMap queryParams
	) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ArrayList<ImageModificationDto> imageModificationDtos = new ArrayList<ImageModificationDto>();

		for ( int n = 0; n < queryParams.get( "resolution.width" ).size(); ++n ) {
			ImageModificationDto modification = new ImageModificationDto();

			ImageResolutionDto resolution = new ImageResolutionDto();
			resolution.setWidth( Integer.parseInt( (String) queryParams.get( "resolution.width" ).get( n ) ) );
			resolution.setHeight(
					Integer.parseInt( (String) queryParams.get( "resolution.height" ).get( n ) ));
			modification.setResolution( resolution );

			CropDto crop = new CropDto();
			crop.setX( Integer.parseInt( (String) queryParams.get( "crop.x" ).get( n ) ) );
			crop.setY( Integer.parseInt( (String) queryParams.get( "crop.y" ).get( n ) ) );
			crop.setWidth( Integer.parseInt( (String) queryParams.get( "crop.width" ).get( n ) ) );
			crop.setHeight( Integer.parseInt( (String) queryParams.get( "crop.height" ).get( n ) ) );
			modification.setCrop( crop );

			DimensionsDto source = new DimensionsDto();
			source.setWidth( Integer.parseInt( (String) queryParams.get( "crop.source.width" ).get( n ) ) );
			source.setHeight( Integer.parseInt( (String) queryParams.get( "crop.source.height" ).get( n ) ) );
			crop.setSource( source );

			DimensionsDto box = new DimensionsDto();
			box.setWidth( Integer.parseInt( (String) queryParams.get( "crop.box.width" ).get( n ) ) );
			box.setHeight( Integer.parseInt( (String) queryParams.get( "crop.box.height" ).get( n ) ) );
			crop.setBox( box );

			DimensionsDto density = new DimensionsDto();
			density.setWidth( Integer.parseInt( (String) queryParams.get( "density.width" ).get( n ) ) );
			density.setHeight( Integer.parseInt( (String) queryParams.get( "density.height" ).get( n ) ) );
			modification.setDensity( density );

			DimensionsDto boundaries = new DimensionsDto();
			boundaries.setWidth( Integer.parseInt( (String) queryParams.get( "boundaries.width" ).get( n ) ) );
			boundaries.setHeight( Integer.parseInt( (String) queryParams.get( "boundaries.height" ).get( n ) ) );
			modification.setBoundaries( boundaries );

			imageModificationDtos.add( modification );
		}

		RegisterModificationRequest request = new RegisterModificationRequest();
		request.setExternalId( externalId );
		request.setContext( contextCode );
		request.setImageModificationDtos( imageModificationDtos );

		RegisterModificationResponse response = imageRestService.registerModifications( request );

		if ( response.isContextDoesNotExist() ) {
			return error( "No such context." );
		}

		if ( response.isImageDoesNotExist() ) {
			return error( String.format( "No image available for identifier %s.", request.getExternalId() ) );
		}

		if ( response.isResolutionDoesNotExist() ) {
			StringBuilder errorMessage = new StringBuilder();
			for ( ImageResolutionDto imageResolutionDto : response.getMissingResolutions() ) {
				errorMessage.append(
						"No such image resolution : " + imageResolutionDto.getWidth() + "x" + imageResolutionDto.getHeight() + "\r\n" );
			}
			return error( StringUtils.substringBeforeLast( errorMessage.toString(), "\r\n" ) );
		}

		if ( response.isCropOutsideOfImageBounds() ) {
			return error( "Crop dimensions fall outside image bounds." );
		}

		return success();
	}

	@RequestMapping(value = LIST_MODIFICATIONS, method = RequestMethod.GET)
	public JsonResponse listModifications( @RequestParam(value = "token", required = true) String accessToken,
	                                       @RequestParam(value = "iid", required = true) String externalId,
	                                       @RequestParam(value = "context", required = true) String contextCode
	) {
		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		ListModificationsRequest listModificationsRequest = new ListModificationsRequest();
		listModificationsRequest.setExternalId( externalId );
		listModificationsRequest.setContext( contextCode );

		ListModificationsResponse response = imageRestService.listModifications( listModificationsRequest );

		if ( response.isContextDoesNotExist() ) {
			return error( "No such context." );
		}

		if ( response.isImageDoesNotExist() ) {
			return error( String.format( "No image available for identifier %s.",
			                             listModificationsRequest.getExternalId() ) );
		}

		return success( response.getModifications() );
	}
}
