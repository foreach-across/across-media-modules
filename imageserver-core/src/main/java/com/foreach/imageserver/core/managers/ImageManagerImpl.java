package com.foreach.imageserver.core.managers;

import com.foreach.across.modules.hibernate.services.HibernateSessionHolder;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

@Repository
public class ImageManagerImpl implements ImageManager
{
	private static final String CACHE_NAME = "images";

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private HibernateSessionHolder hibernateSessionHolder;

	@Override
	@Cacheable(value = CACHE_NAME, key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byIdKey(#imageId)",
			unless = "#result == null")
	public Image getById( long imageId ) {
		return imageRepository.findOne( imageId );
	}

	@Override
	@Cacheable(value = CACHE_NAME,
			key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byExternalIdKey(#externalId)",
			unless = "#result == null")
	public Image getByExternalId( String externalId ) {
		return imageRepository.getByExternalId( externalId );
	}

	@Override
	public void insert( Image image ) {
		imageRepository.create( image );
	}

	@Override
	@Caching(evict = {
			@CacheEvict(
					value = CACHE_NAME,
					key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byExternalIdKey(#image.externalId)"
			),
			@CacheEvict(
					value = CACHE_NAME,
					key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byIdKey(#image.id)"
			)
	})
	public void delete( Image image ) {
		imageRepository.delete( image );
		hibernateSessionHolder.getCurrentSession().flush();
	}

	public static String byIdKey( int imageId ) {
		return "byId-" + imageId;
	}

	public static String byExternalIdKey( String externalId ) {
		return "byExternalId-" + externalId;
	}
}
