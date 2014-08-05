package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ImageResolutionManagerImpl implements ImageResolutionManager
{
	private static final String CACHE_NAME = "imageResolutions";

	private final ImageResolutionDao imageResolutionDao;

	@Autowired
	public ImageResolutionManagerImpl( ImageResolutionDao imageResolutionDao ) {
		this.imageResolutionDao = imageResolutionDao;
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'byId-'+#resolutionId")
	public ImageResolution getById( long resolutionId ) {
		return imageResolutionDao.getById( resolutionId );
	}

	@Override
	public ImageResolution getByDimensions( int width, int height ) {
		return imageResolutionDao.getByDimensions( width, height );
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'forContext-'+#contextId")
	public List<ImageResolution> getForContext( long contextId ) {
		return Collections.unmodifiableList( imageResolutionDao.getForContext( contextId ) );
	}

	@Override
	public List<ImageResolution> getAllResolutions() {
		return Collections.unmodifiableList( imageResolutionDao.getAllResolutions() );
	}

	@Override
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void saveResolution( ImageResolution resolution ) {
		if ( resolution.getId() > 0 ) {
			imageResolutionDao.updateResolution( resolution );
		}
		else {
			imageResolutionDao.insertResolution( resolution );
		}
	}
}
