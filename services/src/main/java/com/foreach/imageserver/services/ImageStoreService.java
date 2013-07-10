package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;

import java.io.InputStream;

public interface ImageStoreService
{
	String generateRelativeImagePath( Image image );

	long saveImage( Image image, InputStream imageData );

	void deleteVariants( Image image );

	ImageFile getImageFile( Image image );
}
