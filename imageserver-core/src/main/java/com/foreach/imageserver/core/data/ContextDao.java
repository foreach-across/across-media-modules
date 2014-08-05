package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Context;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ContextDao
{
	Context getById( int id );

	Context getByCode( String code );

	Collection<Context> getForResolution( long resolutionId );

	void updateContextsForResolution( @Param("resolutionId") long resolutionId,
	                                  @Param("contexts") Collection<Context> contexts );

	Collection<Context> getAllContexts();
}
