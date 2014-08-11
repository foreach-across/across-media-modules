package com.foreach.imageserver.core.client;

import com.foreach.imageserver.client.AbstractImageServerClient;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.ImageServerException;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.*;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * ImageServerClient that accesses the core services directly, instead of through
 * the remote endpoint.
 *
 * @author Arne Vandamme
 */
public class LocalImageServerClient extends AbstractImageServerClient implements ImageServerClient
{
	@Autowired
	private ImageRestService imageRestService;

	@Autowired
	private ImageService imageService;

	public LocalImageServerClient( String imageServerUrl ) {
		super( imageServerUrl );
	}

	@Override
	public InputStream imageStream( String imageId,
	                                String context,
	                                Integer width,
	                                Integer height,
	                                ImageTypeDto imageType ) {
		return imageStream( imageId, context, new ImageResolutionDto( width, height ),
		                    new ImageVariantDto( imageType ) );
	}

	@Override
	public InputStream imageStream( String imageId,
	                                String context,
	                                ImageResolutionDto imageResolution,
	                                ImageVariantDto imageVariant ) {
		ViewImageRequest request = new ViewImageRequest();
		request.setExternalId( imageId );
		request.setContext( context );
		request.setImageResolutionDto( imageResolution );
		request.setImageVariantDto( imageVariant );

		ViewImageResponse response = imageRestService.viewImage( request );

		if ( response.isImageDoesNotExist() ) {
			throw new ImageServerException( "No such image." );
		}
		else if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "No such context." );
		}
		else if ( response.isResolutionDoesNotExist() ) {
			throw new ImageServerException( "No such resolution." );
		}
		else if ( response.isFailed() ) {
			throw new ImageServerException( "Could not create variant." );
		}

		return response.getImageSource().getImageStream();
	}

	@Override
	public InputStream imageStream( String imageId,
	                                ImageModificationDto imageModificationDto,
	                                ImageVariantDto imageVariant ) {
		ViewImageRequest request = new ViewImageRequest();
		request.setExternalId( imageId );
		request.setImageModificationDto( imageModificationDto );
		request.setImageVariantDto( imageVariant );

		ViewImageResponse response = imageRestService.renderImage( request );

		if ( response.isImageDoesNotExist() ) {
			throw new ImageServerException( "No such image." );
		}
		else if ( response.isFailed() ) {
			throw new ImageServerException( "Could not create variant." );
		}

		return response.getImageSource().getImageStream();
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes ) {
		return loadImage( imageId, imageBytes, null );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate ) {
		Image image = imageService.saveImage( imageId, imageBytes, imageDate != null ? imageDate : new Date() );
		return DtoUtil.toDto( image );
	}

	@Override
	public boolean imageExists( String imageId ) {
		Image image = imageService.getByExternalId( imageId );

		return image != null;
	}

	@Override
	public ImageInfoDto imageInfo( String imageId ) {
		Image image = imageService.getByExternalId( imageId );

		if ( image == null ) {
			ImageInfoDto imageInfoDto = new ImageInfoDto();
			imageInfoDto.setExternalId( imageId );
			imageInfoDto.setExisting( false );

			return imageInfoDto;
		}

		return DtoUtil.toDto( image );
	}

	@Override
	public List<ImageResolutionDto> pregenerateResolutions( String imageId ) {
		PregenerateResolutionsResponse response = imageRestService.pregenerateResolutions( imageId );

		if ( response.isImageDoesNotExist() ) {
			throw new ImageServerException( "Image does not exist: " + imageId );
		}

		return response.getImageResolutions();
	}

	@Override
	public List<ImageResolutionDto> listAllowedResolutions( String context ) {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContext( context );

		ListResolutionsResponse response = imageRestService.listResolutions( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}

		return response.getImageResolutions();
	}

	@Override
	public List<ImageResolutionDto> listConfigurableResolutions( String context ) {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContext( context );
		request.setConfigurableOnly( true );

		ListResolutionsResponse response = imageRestService.listResolutions( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}

		return response.getImageResolutions();
	}

	@Override
	public void registerImageModification( String imageId,
	                                       String context,
	                                       ImageModificationDto imageModificationDto ) {
		RegisterModificationRequest request = new RegisterModificationRequest();
		request.setExternalId( imageId );
		request.setContext( context );
		request.setImageModificationDto( imageModificationDto );

		RegisterModificationResponse response = imageRestService.registerModification( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}

		if ( response.isImageDoesNotExist() ) {
			throw new ImageServerException( "Image does not exist: " + imageId );
		}

		if ( response.isResolutionDoesNotExist() ) {
			throw new ImageServerException(
					"No such image resolution : " + imageModificationDto.getResolution().getWidth() + "x" + imageModificationDto.getResolution().getHeight() );
		}

		if ( response.isCropOutsideOfImageBounds() ) {
			throw new ImageServerException( "Crop dimensions fall outside image bounds." );
		}
	}

	@Override
	public List<ImageModificationDto> listModifications( String imageId, String context ) {
		ListModificationsRequest request = new ListModificationsRequest();
		request.setExternalId( imageId );
		request.setContext( context );

		ListModificationsResponse response = imageRestService.listModifications( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}
		if ( response.isImageDoesNotExist() ) {
			throw new ImageServerException( "Image does not exist: " + imageId );
		}

		return response.getModifications();
	}
}
