package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.response.ListModificationsResponse;
import com.foreach.imageserver.core.rest.response.RegisterModificationResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@ImageServerController
@ResponseBody
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController
{
	public static final String REGISTER_PATH = "register";
	public static final String LIST_MODIFICATIONS = "listModifications";

	@Autowired
	private ImageRestService imageRestService;

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

		RegisterModificationRequest request = new RegisterModificationRequest();
		request.setExternalId( externalId );
		request.setContext( contextCode );
		request.setImageModificationDto( imageModificationDto );

		RegisterModificationResponse response = imageRestService.registerModification( request );

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

	@RequestMapping(value = "/" + LIST_MODIFICATIONS, method = RequestMethod.GET)
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
