package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import com.foreach.imageserver.dto.ImageContextDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class ImageContextManagerImpl implements ImageContextManager
{
	private static final String CACHE_NAME = "contexts";

	@Autowired
	private ImageContextRepository contextRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byCode-'+#code")
	public ImageContext getByCode( String code ) {
		return contextRepository.getByCode( code );
	}

	@Override
	@Cacheable(value = CACHE_NAME)
	public Collection<ImageContext> getAllContexts() {
		return contextRepository.getAll();
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void save( ImageContextDto contextDto ) {
		ImageContext context;

		if ( contextDto.isNewEntity() ) {
			context = new ImageContext();
		}
		else {
			context = getByCode( contextDto.getCode() );
		}

		BeanUtils.copyProperties( contextDto, context );

		if ( contextDto.isNewEntity() ) {
			contextRepository.create( context );
		}
		else {
			contextRepository.update( context );
		}

		BeanUtils.copyProperties( context, contextDto );
	}
}
