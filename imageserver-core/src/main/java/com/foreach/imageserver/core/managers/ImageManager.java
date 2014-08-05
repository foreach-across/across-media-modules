package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Image;

public interface ImageManager
{
	Image getById( long imageId );

	Image getByExternalId( String externalId );

	void insert( Image image );
}
