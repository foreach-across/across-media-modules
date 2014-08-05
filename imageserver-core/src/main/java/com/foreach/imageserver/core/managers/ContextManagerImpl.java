package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.data.ContextDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class ContextManagerImpl implements ContextManager
{
	private final static String CACHE_NAME = "contexts";

	private final ContextDao contextDao;

	@Autowired
	public ContextManagerImpl( ContextDao contextDao, CacheManager cacheManager ) {
		this.contextDao = contextDao;

		Cache cache = cacheManager.getCache( CACHE_NAME );
		if ( cache == null ) {
			throw new RuntimeException( String.format( "Required cache %s is unavailable.", CACHE_NAME ) );
		}
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byCode-'+#code")
	public Context getByCode( String code ) {
		return contextDao.getByCode( code );
	}

	@Override
	public Collection<Context> getForResolution( int resolutionId ) {
		return contextDao.getForResolution( resolutionId );
	}

	@Override
	@Cacheable(value = CACHE_NAME)
	public Collection<Context> getAllContexts() {
		return contextDao.getAllContexts();
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void updateContextsForResolution( int resolutionId, Collection<Context> contexts ) {
		contextDao.updateContextsForResolution( resolutionId, contexts );
	}
}
