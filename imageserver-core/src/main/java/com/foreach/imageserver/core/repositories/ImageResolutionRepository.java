package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageResolution;

import java.util.List;

public interface ImageResolutionRepository extends BasicRepository<ImageResolution>
{
	List<ImageResolution> getForContext( long contextId );

	ImageResolution getByDimensions( int width, int height );
}
