package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import com.foreach.imageserver.dto.ImageContextDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class ImageContextManagerImpl implements ImageContextManager
{
	private static final String CACHE_NAME = "contexts";

	private final ImageContextRepository contextRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byCode-'+#code")
	public ImageContext getByCode( String code ) {
		return contextRepository.getByCode( code );
	}

	@Override
	@Cacheable(value = CACHE_NAME)
	public Collection<ImageContext> getAllContexts() {
		return contextRepository.findAll();
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void save( ImageContextDto contextDto ) {
		ImageContext context;

		if ( contextDto.isNew() ) {
			context = new ImageContext();
		}
		else {
			context = getByCode( contextDto.getCode() );
		}

		BeanUtils.copyProperties( contextDto, context );

		contextRepository.save( context );

		BeanUtils.copyProperties( context, contextDto );
	}
}
