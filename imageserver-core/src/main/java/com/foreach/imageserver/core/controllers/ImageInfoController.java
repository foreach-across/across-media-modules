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

import static com.foreach.imageserver.core.controllers.ImageLoadController.IMAGE_INFO_PATH;

/**
 * @author Gunther Van Geetsom
 * @since 5.0.0
 */
@ImageServerController
public class ImageInfoController extends BaseImageAPIController
{

	@Autowired
	private ImageService imageService;

	public ImageInfoController( String accessToken ) {
		super( accessToken );
	}

	@InitBinder
	public void initBinder( ServletRequestDataBinder binder ) {
		binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor() );
	}

	@RequestMapping(value = IMAGE_INFO_PATH, method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse imageInfo( @RequestParam(value = "token", required = true) String accessToken,
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
