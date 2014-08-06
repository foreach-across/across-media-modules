package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.repositories.ContextRepository;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class ContextManagerImpl implements ContextManager
{
	private final static String CACHE_NAME = "contexts";

	@Autowired
	private ContextRepository contextRepository;
	@Autowired
	private ImageResolutionRepository imageResolutionRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byCode-'+#code")
	public Context getByCode( String code ) {
		return contextRepository.getByCode( code );
	}

	@Override
	public Collection<Context> getForResolution( long resolutionId ) {
		return contextRepository.getForResolution( resolutionId );
	}

	@Override
	@Cacheable(value = CACHE_NAME)
	public Collection<Context> getAllContexts() {
		return contextRepository.getAll();
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void updateContextsForResolution( long resolutionId, Collection<Context> contexts ) {
		imageResolutionRepository.updateContextsForResolution( resolutionId, contexts );
	}
}
