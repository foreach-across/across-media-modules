package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.dto.ImageModificationDto;

import java.util.List;

public interface CropGenerator
{

	Crop generateCrop( Image image,
	                   ImageContext context,
	                   ImageResolution resolution,
	                   List<ImageModification> modifications );

	ImageModificationDto buildModificationDto( Image image, ImageContext context, ImageResolution imageResolution );
}
