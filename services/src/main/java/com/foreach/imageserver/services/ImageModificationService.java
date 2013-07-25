package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public interface ImageModificationService
{
	Dimensions calculateDimensions( ImageFile file );

	ImageFile apply( ImageFile original, ImageModifier modifier );
}
