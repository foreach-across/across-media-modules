package com.foreach.imageserver.admin.rendering;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.paths.ImageSpecifier;

public interface ImageRenderingFacade
{
	ImageRenderingResult generateVariant( ServableImageData imageData, ImageSpecifier imageSpecifier );
}
