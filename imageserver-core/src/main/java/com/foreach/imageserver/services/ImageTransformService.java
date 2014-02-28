package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModifier;

public interface ImageTransformService
{
	Dimensions calculateDimensions( ImageFile file );

	ImageFile apply( ImageFile original, ImageModifier modifier );
}
