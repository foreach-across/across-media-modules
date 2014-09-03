package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class ImageManagerImpl implements ImageManager
{
	private static final String CACHE_NAME = "images";

	@Autowired
	private ImageRepository imageRepository;

	@Override
	@Cacheable(value = CACHE_NAME, key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byIdKey(#imageId)",
	           unless = "#result == null")
	public Image getById( long imageId ) {
		return imageRepository.getById( imageId );
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

	public static String byIdKey( int imageId ) {
		return "byId-" + imageId;
	}

	public static String byExternalIdKey( String externalId ) {
		return "byExternalId-" + externalId;
	}
}
