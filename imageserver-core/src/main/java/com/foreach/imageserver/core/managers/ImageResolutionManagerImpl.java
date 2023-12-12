package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageResolutionManagerImpl implements ImageResolutionManager
{
	private static final String CACHE_NAME = "imageResolutions";

	private final ImageResolutionRepository imageResolutionRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byId-'+#resolutionId")
	public Optional<ImageResolution> getById( long resolutionId ) {
		return imageResolutionRepository.findById( resolutionId );
	}

	@Override
	public ImageResolution getByDimensions( int width, int height ) {
		return imageResolutionRepository.getByDimensions( width, height );
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'forContext-'+#contextId")
	public List<ImageResolution> getForContext( long contextId ) {
		return Collections.unmodifiableList( imageResolutionRepository.getForContext( contextId ) );
	}

	@Override
	public Collection<ImageResolution> getAllResolutions() {
		return Collections.unmodifiableCollection( imageResolutionRepository.findAll() );
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void saveResolution( ImageResolution resolution ) {
		imageResolutionRepository.save( resolution );
	}
}
