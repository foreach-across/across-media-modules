package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class ImageResolutionManagerImpl implements ImageResolutionManager
{
	private static final String CACHE_NAME = "imageResolutions";

	@Autowired
	private ImageResolutionRepository imageResolutionRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byId-'+#resolutionId")
	public ImageResolution getById( long resolutionId ) {
		return imageResolutionRepository.getById( resolutionId );
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
		if ( resolution.getId() > 0 ) {
			imageResolutionRepository.update( resolution );
		}
		else {
			imageResolutionRepository.create( resolution );
		}
	}
}
