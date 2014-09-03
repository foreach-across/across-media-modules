package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepository;
import com.foreach.imageserver.core.business.ImageContext;

public interface ImageContextRepository extends BasicRepository<ImageContext>
{
	ImageContext getByCode( String code );
}
