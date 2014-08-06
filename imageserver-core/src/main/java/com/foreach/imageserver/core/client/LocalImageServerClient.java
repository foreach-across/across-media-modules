package com.foreach.imageserver.core.client;

import be.mediafin.imageserver.client.AbstractImageServerClient;
import be.mediafin.imageserver.client.ImageServerClient;
import be.mediafin.imageserver.client.ImageServerException;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ResolutionRestService;
import com.foreach.imageserver.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	public DimensionsDto loadImage( String imageId, byte[] imageBytes ) {
		return null;
	}

	@Override
	public DimensionsDto loadImage( String imageId, byte[] imageBytes, Date imageDate ) {
		return null;
	}

	@Override
	public boolean imageExists( String imageId ) {
		return false;
	}

	@Override
	public ImageInfoDto imageInfo( String imageId ) {
		return null;
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
		request.setContextCode( context );

		ListResolutionsResponse response = resolutionRestService.listResolutions( request );

		if ( response.isContextDoesNotExist() ) {
			throw new ImageServerException( "Context does not exist: " + context );
		}

		return response.getImageResolutions();
	}

	@Override
	public List<ImageResolutionDto> listConfigurableResolutions( String context ) {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContextCode( context );
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
