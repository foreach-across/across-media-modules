package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface ImageServerClient
{
	Logger LOG = LoggerFactory.getLogger( ImageServerClient.class );

	String ENDPOINT_IMAGE_VIEW = "view";
	String ENDPOINT_IMAGE_RENDER = "api/image/render";
	String ENDPOINT_IMAGE_LOAD = "api/image/load";
	String ENDPOINT_IMAGE_INFO = "api/image/details";
	String ENDPOINT_IMAGE_PREGENERATE = "api/image/pregenerate";
	String ENDPOINT_RESOLUTION_LIST = "api/resolution/list";
	String ENDPOINT_MODIFICATION_LIST = "api/modification/list";
	String ENDPOINT_MODIFICATION_REGISTER = "api/modification/register";

	String getImageServerUrl();

	String imageUrl( String imageId,
	                 String context,
	                 int width,
	                 int height );

	String imageUrl( String imageId,
	                 String context,
	                 int width,
	                 int height,
	                 ImageTypeDto imageType );

	String imageUrl( String imageId,
	                 String context,
	                 ImageResolutionDto imageResolution,
	                 ImageVariantDto imageVariant );

	InputStream imageStream( String imageId,
	                         String context,
	                         Integer width,
	                         Integer height,
	                         ImageTypeDto imageType );

	InputStream imageStream( String imageId,
	                         String context,
	                         ImageResolutionDto imageResolution,
	                         ImageVariantDto imageVariant );

	InputStream imageStream( String imageId, ImageModificationDto imageModificationDto, ImageVariantDto imageVariant );

	ImageInfoDto loadImage( String imageId, byte[] imageBytes );

	ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate );

	boolean imageExists( String imageId );

	ImageInfoDto imageInfo( String imageId );

	/**
	 * Will create the variants for all pregenerate resolutions of the image.
	 *
	 * @param imageId External id of the image.
	 * @return List of ImageResolutions that will be pregenerated.
	 */
	List<ImageResolutionDto> pregenerateResolutions( String imageId );

	void registerImageModification( String imageId,
	                                String context,
	                                ImageModificationDto imageModificationDto );

	List<ImageModificationDto> listModifications( String imageId, String context );

	List<ImageResolutionDto> listAllowedResolutions( String context );

	List<ImageResolutionDto> listConfigurableResolutions( String context );
}
