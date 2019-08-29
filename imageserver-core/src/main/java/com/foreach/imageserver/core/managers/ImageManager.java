package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Image;

import java.util.Optional;

public interface ImageManager
{
	Optional<Image> getById( long imageId );

	Image getByExternalId( String externalId );

	void insert( Image image );

	void delete( Image image );
}
