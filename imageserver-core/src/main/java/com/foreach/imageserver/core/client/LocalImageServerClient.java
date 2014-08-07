package com.foreach.imageserver.core.client;

import com.foreach.imageserver.client.AbstractImageServerClient;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.ImageServerException;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ResolutionRestService;
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
	private ResolutionRestService resolutionRestService;

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
		return null;
	}

	@Override
	public InputStream imageStream( String imageId,
	                                String context,
	                                ImageResolutionDto imageResolution,
	                                ImageVariantDto imageVariant ) {
		return null;
	}

	@Override
	public InputStream imageStream( String imageId,
	                                ImageModificationDto imageModificationDto,
	                                ImageVariantDto imageVariant ) {
		return null;
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes ) {
		return null;
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate ) {
		return null;
	}

	@Override
	public boolean imageExists( String imageId ) {
		Image image = imageService.getByExternalId( imageId );

		return image != null;
	}

	@Override
	public ImageInfoDto imageInfo( String imageId ) {
		Image image = imageService.getByExternalId(imageId);

		if ( image == null )
		{
			ImageInfoDto imageInfoDto = new ImageInfoDto();
			imageInfoDto.setExternalId( imageId );
			imageInfoDto.setExisting( false );

			return imageInfoDto;
		}

		return DtoUtil.toDto( image );
	}

	@Override
	public void registerImageModification( String imageId,
	                                       String context,
	                                       ImageModificationDto imageModificationDto ) {

	}

	@Override
	public void registerImageModification( String imageId,
	                                       String context,
	                                       Integer width,
	                                       Integer height,
	                                       int cropX,
	                                       int cropY,
	                                       int cropWidth,
	                                       int croptHeight,
	                                       int densityWidth,
	                                       int densityHeight ) {

	}

	@Override
	public List<ImageResolutionDto> listAllowedResolutions( String context ) {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContext( context );

		ListResolutionsResponse response = resolutionRestService.listResolutions( request );

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

		ListResolutionsResponse response = resolutionRestService.listResolutions( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}

		return response.getImageResolutions();
	}

	@Override
	public List<ImageModificationDto> listModifications( String imageId, String context ) {
		return null;
	}
}
