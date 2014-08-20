package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepository;
import com.foreach.imageserver.core.business.Image;

public interface ImageRepository extends BasicRepository<Image>
{
	Image getByExternalId( String externalId );

	@Override
	void update( Image object );

	@Override
	void create( Image object );
}
