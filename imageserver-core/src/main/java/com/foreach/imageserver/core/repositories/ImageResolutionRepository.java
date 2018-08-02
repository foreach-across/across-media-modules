package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ImageResolutionRepository extends IdBasedEntityJpaRepository<ImageResolution>, ImageResolutionRepositoryCustom
{
	@Query("select distinct r from ImageResolution r join r.contexts c where c.id = :contextId order by r.id")
	List<ImageResolution> getForContext( @Param("contextId") long contextId );

	@Query("select i from ImageResolution i where i.width = :width and i.height = :height")
	ImageResolution getByDimensions( @Param("width") int width, @Param("height") int height );

	void updateContextsForResolution( long resolutionId, Collection<ImageContext> contexts );
}
