package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModifier;

public interface ImageStoreService
{
	String generateRelativeImagePath( Image image );

	String generateFullImagePath( Image image );

	String generateFullImagePath( Image image, ImageModifier modifier );

	ImageFile saveImage( Image image, ImageFile imageFile );

	ImageFile saveImage( Image image, ImageModifier modifier, ImageFile file );

	void delete( Image image );

	void deleteVariants( Image image );

	ImageFile getImageFile( Image image );

	ImageFile getImageFile( Image image, ImageModifier modifier );
}
