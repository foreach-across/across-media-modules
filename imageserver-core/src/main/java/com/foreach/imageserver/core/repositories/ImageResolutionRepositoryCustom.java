package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageContext;

import java.util.Collection;

public interface ImageResolutionRepositoryCustom
{
	void updateContextsForResolution( long resolutionId, Collection<ImageContext> contexts );
}
