package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public interface ImageTransformer
{
	ImageTransformerPriority canApply( ImageFile original, ImageModifier modifier );

	ImageFile apply( ImageFile original, ImageModifier modifier );

	int getPriority();
}
