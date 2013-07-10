package com.foreach.imageserver.services;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public interface ImageModificationService
{
	ImageFile apply( ImageFile original, ImageModifier modifier );
}
