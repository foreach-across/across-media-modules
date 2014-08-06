package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Context;

import java.util.Collection;

public interface ContextRepository extends BasicRepository<Context>
{
	Context getByCode( String code );

	Collection<Context> getForResolution( long resolutionId );


}
