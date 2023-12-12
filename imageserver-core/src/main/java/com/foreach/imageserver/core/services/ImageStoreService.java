package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.transformers.ImageSource;

import java.io.InputStream;

public interface ImageStoreService
{
	void storeOriginalImage( Image image, byte[] imageBytes );

	void storeOriginalImage( Image image, InputStream imageStream );

	ImageSource getOriginalImage( Image image );

	void storeVariantImage( Image image,
	                        ImageContext context,
	                        ImageResolution imageResolution,
	                        ImageVariant imageVariant,
	                        ImageSource imageSource );

	ImageSource getVariantImage( Image image,
	                             ImageContext context,
	                             ImageResolution imageResolution,
	                             ImageVariant imageVariant );

	void removeVariantImage( Image image,
	                         ImageContext context,
	                         ImageResolution imageResolution,
	                         ImageVariant imageVariant );

	void removeVariants( Image image );

	void removeOriginal( Image image );
}
