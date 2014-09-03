package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.ImageContextDto;

import java.util.Collection;
import java.util.List;

public interface ImageContextService
{
	ImageContext getByCode( String contextCode );

	ImageResolution getImageResolution( long contextId, int width, int height );

	List<ImageResolution> getImageResolutions( long contextId );

	Collection<ImageContext> getAllContexts();

	void save( ImageContextDto contextDto );
}
