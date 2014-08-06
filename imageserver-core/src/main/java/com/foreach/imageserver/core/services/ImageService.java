package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ImageService
{
	Image getById( long imageId );

	Image getByExternalId( String externalId );

	Image saveImage( String externalId, byte[] imageBytes, Date imageDate ) throws ImageStoreException;

	void saveImageModification( ImageModification modification );

	void saveImageModification( ImageModification modification, Image image );

	StreamImageSource generateModification( Image image,
	                                        ImageModificationDto modificationDto,
	                                        ImageVariant imageVariant );

	StreamImageSource getVariantImage( Image image,
	                                   Context context,
	                                   ImageResolution imageResolution,
	                                   ImageVariant imageVariant );

	boolean hasModification( int imageId );

	ImageResolution getResolution( long resolutionId );

	ImageResolution getResolution( int width, int height );

	List<ImageModification> getModifications( long imageId, long contextId );

	Collection<ImageResolution> getAllResolutions();

	void saveImageResolution( ImageResolution resolution, Collection<Context> contexts );
}
