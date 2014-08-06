package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Context;

public interface ContextRepository extends BasicRepository<Context>
{
	Context getByCode( String code );
}
