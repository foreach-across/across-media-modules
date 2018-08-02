package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.imageserver.core.business.Image;

public interface ImageRepository extends IdBasedEntityJpaRepository<Image>, ImageRepositoryCustom
{
	Image getByExternalId( String externalId );
}
