package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageInfoDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import static com.foreach.imageserver.core.controllers.ImageInfoController.IMAGE_INFO_PATH;

/**
 * @author Gunther Van Geetsom
 * @since 5.0.0
 */
@ImageServerController
@RequestMapping(value = IMAGE_INFO_PATH)
public class ImageInfoController extends BaseImageAPIController
{

	public static final String IMAGE_INFO_PATH = "/api/image/details";

	@Autowired
	private ImageService imageService;

	public ImageInfoController( String accessToken ) {
		super( accessToken );
	}

	@InitBinder
	public void initBinder( ServletRequestDataBinder binder ) {
		binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor() );
	}

	@GetMapping
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

	@PostMapping
	@ResponseBody
	public JsonResponse infoForUploadedImage( @RequestParam(value = "token", required = true) String accessToken,
	                                          @RequestParam(value = "imageData", required = true) byte[] imageData ) {

		if ( !this.accessToken.equals( accessToken ) ) {
			return error( "Access denied." );
		}

		Image image = imageService.loadImageData( imageData );
		ImageInfoDto imageInfoDto = DtoUtil.toDto( image );
		imageInfoDto.setExisting( false );

		return success( imageInfoDto );
	}
}
