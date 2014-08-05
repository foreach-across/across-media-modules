package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Image;

public interface ImageRepository extends BasicRepository<Image>
{
	Image getByExternalId( String externalId );
}
