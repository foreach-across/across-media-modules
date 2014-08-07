package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface ImageServerClient
{
	String ENDPOINT_IMAGE_VIEW = "view";
	String ENDPOINT_IMAGE_RENDER = "render";
	String ENDPOINT_IMAGE_LOAD = "load";
	String ENDPOINT_IMAGE_INFO = "imageInfo";
	String ENDPOINT_RESOLUTION_LIST = "resolutions/list";
	String ENDPOINT_MODIFICATION_LIST = "modification/listModifications";
	String ENDPOINT_MODIFICATION_REGISTER = "modification/register";

	String getImageServerUrl();

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

	void registerImageModification( String imageId,
	                                String context,
	                                ImageModificationDto imageModificationDto );

	void registerImageModification( String imageId,
	                                String context,
	                                Integer width,
	                                Integer height,
	                                int cropX,
	                                int cropY,
	                                int cropWidth,
	                                int croptHeight,
	                                int densityWidth,
	                                int densityHeight );

	List<ImageModificationDto> listModifications( String imageId, String context );

	List<ImageResolutionDto> listAllowedResolutions( String context );

	List<ImageResolutionDto> listConfigurableResolutions( String context );
}
