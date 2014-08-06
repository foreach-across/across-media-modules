package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;

import java.io.InputStream;

public interface ImageStoreService
{
	void storeOriginalImage( Image image, byte[] imageBytes );

	void storeOriginalImage( Image image, InputStream imageStream );

	StreamImageSource getOriginalImage( Image image );

	void storeVariantImage( Image image,
	                        ImageContext context,
	                        ImageModificationDto modification,
	                        ImageVariant imageVariant,
	                        InputStream imageStream );

	StreamImageSource getVariantImage( Image image,
	                                   ImageContext context,
	                                   ImageModificationDto modification,
	                                   ImageVariant imageVariant );

	void removeVariantImage( Image image,
	                         ImageContext context,
	                         ImageModificationDto modification,
	                         ImageVariant imageVariant );

	void removeVariants( long imageId );
}
