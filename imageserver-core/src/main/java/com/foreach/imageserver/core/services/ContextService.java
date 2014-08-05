package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ImageResolution;

import java.util.Collection;
import java.util.List;

public interface ContextService
{
	Context getByCode( String contextCode );

	ImageResolution getImageResolution( long contextId, int width, int height );

	List<ImageResolution> getImageResolutions( long contextId );

	Collection<Context> getForResolution( long resolutionId );

	Collection<Context> getAllContexts();
}
