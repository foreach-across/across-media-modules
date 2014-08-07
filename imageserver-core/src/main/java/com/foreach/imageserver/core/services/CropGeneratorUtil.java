package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.ImageModificationDto;

/**
 * Created by marc on 7/08/2014.
 */
public interface CropGeneratorUtil
{
	void normalizeModificationDto( Image image, ImageModificationDto imageModificationDto );

	Dimensions applyResolution( Image image, ImageResolution resolution );

	int area( Crop crop );

	Crop intersect( Crop crop1, Crop crop2 );
}
