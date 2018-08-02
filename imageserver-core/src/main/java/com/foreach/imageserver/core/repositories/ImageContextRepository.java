package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.imageserver.core.business.ImageContext;

public interface ImageContextRepository extends IdBasedEntityJpaRepository<ImageContext>
{
	ImageContext getByCode( String code );
}
