package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Context;

import java.util.Collection;

public interface ContextManager
{
	Context getByCode( String code );

	Collection<Context> getAllContexts();

	Collection<Context> getForResolution( long resolutionId );

	void updateContextsForResolution( long resolutionId, Collection<Context> contexts );
}
