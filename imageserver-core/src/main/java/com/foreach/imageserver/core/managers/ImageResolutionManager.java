package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ImageResolutionManager
{
	Optional<ImageResolution> getById( long resolutionId );

	List<ImageResolution> getForContext( long contextId );

	Collection<ImageResolution> getAllResolutions();

	void saveResolution( ImageResolution resolution );

	ImageResolution getByDimensions( int width, int height );
}
