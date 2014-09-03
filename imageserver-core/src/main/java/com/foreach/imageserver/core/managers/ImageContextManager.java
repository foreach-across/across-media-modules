package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.dto.ImageContextDto;

import java.util.Collection;

public interface ImageContextManager
{
	ImageContext getByCode( String code );

	Collection<ImageContext> getAllContexts();

	void save( ImageContextDto contextDto );
}
